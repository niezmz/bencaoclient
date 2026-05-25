package com.example.bencaoclient

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 本草物种 **唯一** 备份格式：单文件 ZIP。
 *
 * 结构：
 * - [MANIFEST_ENTRY]：UTF-8 JSON（`formatVersion`、`schema`、`species[]`，每条 `images` 为 ZIP 内相对路径）
 * - `media/s{n}/i{m}.xxx`：图片二进制
 *
 * 可选字段缺失或单资源失败时不中止整体导出/导入（尽力而为；图片缺失则该项留空）。
 *
 * [exportZip]、[importZip] 均为阻塞调用，须在后台线程执行；且 **SAF 的 openOutputStream/openInputStream 与读写须在一线程**，
 * 否则部分机型会得到 0 字节文件。
 */
object BencaoSpeciesBackupZip {

    const val MANIFEST_ENTRY = "species.json"
    const val BUNDLE_KIND = "bencaoclient_species_archive_v1"

    /** 当前唯一的备份清单版本（仅出现在 ZIP 内的 JSON 中） */
    const val FORMAT_VERSION: Int = 1

    /** 跨客户端识别用 schema，写入 manifest */
    const val SCHEMA_ID: String = "com.example.bencaoclient.species.v1"

    /**
     * @param onProgress 可选进度：(已完成步数, 总步数)。总步数 = 全部物种的图片路径条数 + 1（写入清单）。
     *  回调可能在后台线程触发，更新 UI 时请切回主线程。
     */
    fun exportZip(
        context: Context,
        species: List<Bencao>,
        outputStream: OutputStream,
        onProgress: ((completed: Int, total: Int) -> Unit)? = null
    ) {
        val totalSteps = species.sumOf { it.images.size } + 1
        var completed = 0
        fun bump() {
            completed++
            onProgress?.invoke(completed, totalSteps)
        }

        onProgress?.invoke(0, totalSteps)

        ZipOutputStream(BufferedOutputStream(outputStream)).use { zos ->
            val speciesArr = JSONArray()
            val exportedAt = System.currentTimeMillis()

            species.forEachIndexed { speciesIdx, b ->
                val refs = mutableListOf<String>()
                b.images.forEachIndexed { imgIdx, uriStr ->
                    try {
                        val uri = runCatching { Uri.parse(uriStr.trim()) }.getOrNull()
                        if (uri != null) {
                            val stream = openUriInputStream(context, uri)
                            if (stream != null) {
                                val ext = guessImageExtension(context, uri)
                                val entryPath = "media/s$speciesIdx/i$imgIdx$ext"
                                try {
                                    zos.putNextEntry(ZipEntry(entryPath))
                                    stream.use { inp -> inp.copyTo(zos) }
                                    refs.add(entryPath)
                                } catch (_: Exception) {
                                    // 单张失败不影响本条其余图片与其它物种
                                } finally {
                                    runCatching { zos.closeEntry() }
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // 忽略单张错误
                    } finally {
                        bump()
                    }
                }
                speciesArr.put(b.toManifestSpeciesJson(refs))
            }

            val root = JSONObject()
                .put("formatVersion", FORMAT_VERSION)
                .put("schema", SCHEMA_ID)
                .put("bundleKind", BUNDLE_KIND)
                .put("exportedAtEpochMs", exportedAt)
                .put("species", speciesArr)

            zos.putNextEntry(ZipEntry(MANIFEST_ENTRY))
            zos.write(root.toString(2).toByteArray(Charsets.UTF_8))
            zos.closeEntry()
            bump()
        }
    }

    /**
     * @param onProgress 可选进度：
     *  - `(0, 0)`：正在解压（总量未知，界面应用不定进度）；
     *  - `(done, total)` 且 `total > 0`：解压完成后的确定性进度，总步数 = 每条物种的图片路径数 + 每条物种解析各一步。
     */
    fun importZip(
        context: Context,
        zipInputStream: InputStream,
        onProgress: ((completed: Int, total: Int) -> Unit)? = null
    ): Result<List<Bencao>> {
        return runCatching {
            onProgress?.invoke(0, 0)

            val tmpDir = File(context.cacheDir, "species_zip_${UUID.randomUUID()}").apply { mkdirs() }
            try {
                unzipToDirectory(zipInputStream, tmpDir)
                val manifest = File(tmpDir, MANIFEST_ENTRY)
                if (!manifest.exists()) error("ZIP 内缺少 $MANIFEST_ENTRY")
                val root = JSONObject(manifest.readText(Charsets.UTF_8))
                // formatVersion / schema 仅作提示，不匹配仍尽力解析
                val arr = root.optJSONArray("species") ?: JSONArray()

                var speciesRows = 0
                var imageSlots = 0
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    speciesRows++
                    imageSlots += readImagesFromManifest(o).size
                }
                val totalSteps = speciesRows + imageSlots
                var completed = 0
                fun bump() {
                    completed++
                    if (totalSteps > 0) {
                        onProgress?.invoke(completed, totalSteps)
                    }
                }

                if (totalSteps == 0) {
                    onProgress?.invoke(1, 1)
                    return@runCatching emptyList()
                }
                onProgress?.invoke(0, totalSteps)

                val batchId = UUID.randomUUID().toString()
                val mediaRoot = File(context.filesDir, "species_media/$batchId").apply { mkdirs() }
                var imgCounter = 0

                buildList {
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue
                        val relList = readImagesFromManifest(o)
                        val permanentPaths = buildList {
                            for (rel in relList) {
                                try {
                                    if (!rel.isBlank()) {
                                        val src = runCatching { File(tmpDir, rel).canonicalFile }.getOrNull()
                                        if (src != null && src.exists()) {
                                            runCatching {
                                                val safeName = "${imgCounter++}_${src.name.ifBlank { "img.bin" }}"
                                                val dest = File(mediaRoot, safeName)
                                                src.copyTo(dest, overwrite = true)
                                                add(dest.absolutePath)
                                            }
                                        }
                                    }
                                } finally {
                                    bump()
                                }
                            }
                        }
                        add(parseSpeciesFromManifest(o, permanentPaths))
                        bump()
                    }
                }
            } finally {
                tmpDir.deleteRecursively()
            }
        }
    }

    private fun Bencao.toManifestSpeciesJson(imageRefs: List<String>): JSONObject =
        JSONObject()
            .put("sourceId", id)
            .put("name", name)
            .put("family", family)
            .put("genus", genus)
            .put("species", species)
            .put("description", description)
            .put("images", JSONArray(imageRefs))
            .put("rarity", rarity)
            .put("isToxic", isToxic)
            .put("isProtectedSpecies", isProtectedSpecies)
            .put("isInvasiveSpecies", isInvasiveSpecies)
            .put("plantingMethod", plantingMethod)
            .put("isSuccess", isSuccess)
            .put("createdAtEpochMs", createdAt.time)

    private fun parseSpeciesFromManifest(o: JSONObject, resolvedImagePaths: List<String>): Bencao =
        Bencao(
            id = 0L,
            name = o.optString("name", "").ifBlank { "未命名" },
            family = o.optString("family", ""),
            genus = o.optString("genus", ""),
            species = o.optString("species", ""),
            description = o.optString("description", ""),
            images = resolvedImagePaths.take(10),
            rarity = o.optInt("rarity", 1).coerceIn(1, 5),
            isToxic = o.optBoolean("isToxic", false),
            isProtectedSpecies = o.optBoolean("isProtectedSpecies", false),
            isInvasiveSpecies = o.optBoolean("isInvasiveSpecies", false),
            plantingMethod = o.optString("plantingMethod", ""),
            isSuccess = o.optBoolean("isSuccess", false),
            createdAt = Date(o.optLong("createdAtEpochMs", System.currentTimeMillis()))
        )

    private fun readImagesFromManifest(o: JSONObject): List<String> {
        val arr = o.optJSONArray("images") ?: JSONArray()
        return buildList {
            for (i in 0 until arr.length()) {
                add(arr.optString(i))
            }
        }.take(10)
    }

    private fun unzipToDirectory(rawZipStream: InputStream, destDir: File) {
        val destCanonical = destDir.canonicalFile
        ZipInputStream(BufferedInputStream(rawZipStream)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val outFile = File(destDir, entry.name).canonicalFile
                val destPath = destCanonical.canonicalPath + File.separator
                val outPath = outFile.canonicalPath
                if (!outPath.startsWith(destPath) && outPath != destCanonical.canonicalPath) {
                    zis.copyTo(BlackHoleOutputStream)
                    zis.closeEntry()
                    entry = zis.nextEntry
                    continue
                }
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos -> zis.copyTo(fos) }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private fun openUriInputStream(context: Context, uri: Uri): InputStream? {
        return try {
            when (uri.scheme?.lowercase(Locale.ROOT)) {
                "content" -> context.contentResolver.openInputStream(uri)
                "file" -> uri.path?.let { p -> File(p).takeIf { it.isFile }?.inputStream() }
                else -> {
                    uri.path?.let { p ->
                        File(p).takeIf { it.isFile }?.inputStream()
                    } ?: File(uri.toString()).takeIf { it.isFile }?.inputStream()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun guessImageExtension(context: Context, uri: Uri): String {
        val mime = context.contentResolver.getType(uri)?.lowercase(Locale.ROOT)
        return when {
            mime?.contains("png") == true -> ".png"
            mime?.contains("webp") == true -> ".webp"
            mime?.contains("gif") == true -> ".gif"
            mime?.contains("jpeg") == true || mime?.contains("jpg") == true -> ".jpg"
            else -> {
                val path = uri.path ?: uri.toString()
                when {
                    path.endsWith(".png", true) -> ".png"
                    path.endsWith(".webp", true) -> ".webp"
                    path.endsWith(".gif", true) -> ".gif"
                    else -> ".jpg"
                }
            }
        }
    }
}

/** 解压时丢弃 ZipInputStream 当前条目内容（例如跳过路径非法的条目）。 */
private object BlackHoleOutputStream : OutputStream() {
    override fun write(b: Int) {}
    override fun write(b: ByteArray, off: Int, len: Int) {}
}
