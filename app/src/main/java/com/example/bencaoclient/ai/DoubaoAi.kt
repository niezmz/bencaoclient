package com.example.bencaoclient.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import com.example.bencaoclient.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

data class AiBencaoSuggestion(
    val name: String,
    val description: String,
    val family: String = "",
    val genus: String = "",
    val species: String = "",
    val rarity: Int = 1,
    val isToxic: Boolean = false,
    val isProtectedSpecies: Boolean = false,
    val isInvasiveSpecies: Boolean = false
)

object DoubaoAi {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun apiUrl(): String =
        BuildConfig.DOUBAO_BASE_URL.trimEnd('/') + "/chat/completions"

    @Volatile
    var userApiKey: String? = null

    fun setActiveApiKey(key: String?) {
        userApiKey = key?.takeIf { it.isNotBlank() }
    }

    fun getActiveApiKey(): String = userApiKey ?: BuildConfig.DOUBAO_API_KEY

    private fun apiKey(): String = getActiveApiKey()

    private fun model(): String = BuildConfig.DOUBAO_MODEL

    suspend fun analyzeBencaoImage(
        context: Context,
        imageUri: Uri,
        maxDescriptionCodePoints: Int = 128
    ): AiBencaoSuggestion = withContext(Dispatchers.IO) {
        val jpegBytes = transcodeToJpeg(context, imageUri, maxLongEdge = 1024, jpegQuality = 80)
        val base64 = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        val prompt = buildPrompt(maxDescriptionCodePoints)

        val content = JSONArray()
            .put(JSONObject().put("type", "text").put("text", prompt))
            .put(
                JSONObject()
                    .put("type", "image_url")
                    .put("image_url", JSONObject().put("url", "data:image/jpeg;base64,$base64"))
            )

        val messages = JSONArray()
            .put(JSONObject().put("role", "user").put("content", content))

        val body = JSONObject()
            .put("model", model())
            .put("messages", messages)
            .put("temperature", 0.2)
            .toString()

        val request = Request.Builder()
            .url(apiUrl())
            .addHeader("Authorization", "Bearer ${apiKey()}")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody(jsonMediaType))
            .build()

        val responseText = client.newCall(request).execute().use { resp ->
            val txt = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                error("豆包 AI 识图失败：HTTP ${resp.code}. $txt")
            }
            txt
        }

        parseSuggestionFromResponse(responseText, maxDescriptionCodePoints)
    }

    suspend fun fetchPlantingMethod(context: Context, speciesDisplayName: String): String =
        withContext(Dispatchers.IO) {
            val trimmed = speciesDisplayName.trim()
            check(trimmed.isNotEmpty()) { "物种名称为空" }

            val prompt = buildPlantingPrompt(trimmed)

            val messages = JSONArray()
                .put(JSONObject().put("role", "user").put("content", prompt))

            val body = JSONObject()
                .put("model", model())
                .put("messages", messages)
                .put("temperature", 0.3)
                .toString()

            val request = Request.Builder()
                .url(apiUrl())
                .addHeader("Authorization", "Bearer ${apiKey()}")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody(jsonMediaType))
                .build()

            val responseText = client.newCall(request).execute().use { resp ->
                val txt = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    error("获取栽种方式失败：HTTP ${resp.code}. $txt")
                }
                txt
            }

            val root = JSONObject(responseText)
            val choices = root.optJSONArray("choices")
                ?: error("豆包 返回缺少 choices")
            if (choices.length() == 0) error("豆包 返回 choices 为空")
            val content = choices.getJSONObject(0)
                .optJSONObject("message")
                ?.optString("content")
                ?.trim().orEmpty()
            if (content.isEmpty()) error("栽种方式返回为空")
            content
        }

    private fun buildPrompt(maxDescriptionCodePoints: Int): String {
        return """
你是一个植物/本草识别助手。请根据图片识别物种或最接近的常见名称。
要求：
1) 只输出 JSON，对象必须包含 name、description、family、genus、species、rarity、isToxic、isProtectedSpecies、isInvasiveSpecies 字段。
2) name：尽量简短（中文常用名优先，不确定时给出"疑似xxx"）。
3) family/genus/species：植物分类的科、属、种（中文常用分类名，不确定时填空字符串）。
4) description：一句话或两句话概述关键识别特征与常见用途/栖息环境，控制在 ${maxDescriptionCodePoints} 个字以内。
5) rarity：1-5 的整数，1=常见，5=极为稀有。
6) 不要输出除 JSON 以外的任何文字，不要使用 Markdown 代码块。
示例：
{"name":"蒲公英","description":"……","family":"菊科","genus":"蒲公英属","species":"蒲公英","rarity":2,"isToxic":false,"isProtectedSpecies":false,"isInvasiveSpecies":false}
        """.trim()
    }

    private fun buildPlantingPrompt(speciesName: String): String {
        return """
请为以下植物提供详细的栽种/养护方式说明（中文）：
物种名称：$speciesName
要求：
1) 使用 Markdown 格式组织内容，用 #### 四级标题划分光照、温度、水分、土壤、施肥、常见病虫害防治等部分。
2) 语言通俗易懂，适合普通爱好者参考。
3) 控制在 300 字以内。
        """.trim()
    }

    private fun parseSuggestionFromResponse(
        responseText: String,
        maxDescriptionCodePoints: Int
    ): AiBencaoSuggestion {
        val root = JSONObject(responseText)
        val choices = root.optJSONArray("choices") ?: error("豆包 返回缺少 choices")
        if (choices.length() == 0) error("豆包 返回 choices 为空")

        val message = choices.getJSONObject(0).optJSONObject("message")
            ?: error("豆包 返回缺少 message")
        val content = message.optString("content").orEmpty().trim()
        if (content.isBlank()) error("豆包 返回 content 为空")

        val jsonCandidate = extractFirstJsonObject(content) ?: content
        val obj = runCatching { JSONObject(jsonCandidate) }.getOrElse {
            error("无法解析豆包 返回为 JSON：$content")
        }

        return BackendAiSuggestionJson.parseRoot(obj, maxDescriptionCodePoints)
    }

    private fun extractFirstJsonObject(text: String): String? {
        val start = text.indexOf('{')
        if (start < 0) return null
        var depth = 0
        for (i in start until text.length) {
            when (text[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return text.substring(start, i + 1)
                }
            }
        }
        return null
    }

    // --- Image transcoding ---

    private fun transcodeToJpeg(
        context: Context,
        imageUri: Uri,
        maxLongEdge: Int,
        jpegQuality: Int
    ): ByteArray {
        val cr = context.contentResolver
        val imageBytes = cr.openInputStream(imageUri)?.use { it.readBytes() }
            ?: error("无法读取图片：$imageUri")

        val orientation = runCatching {
            ExifInterface(ByteArrayInputStream(imageBytes))
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        }.getOrDefault(ExifInterface.ORIENTATION_UNDEFINED)

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            error("无法读取图片尺寸：$imageUri")
        }

        val srcW = bounds.outWidth.coerceAtLeast(1)
        val srcH = bounds.outHeight.coerceAtLeast(1)
        val longEdge = maxOf(srcW, srcH)
        val sampleSize = computeInSampleSize(longEdge, maxLongEdge)

        val decodeOpts = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, decodeOpts)
            ?: error("无法解码图片：$imageUri")

        val bw = bitmap.width.coerceAtLeast(1)
        val bh = bitmap.height.coerceAtLeast(1)
        val bLong = maxOf(bw, bh)
        if (bLong > maxLongEdge) {
            val scale = maxLongEdge.toFloat() / bLong.toFloat()
            val targetW = (bw * scale).roundToInt().coerceAtLeast(1)
            val targetH = (bh * scale).roundToInt().coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            if (scaled != bitmap) bitmap.recycle()
            bitmap = scaled
        }

        val rotated = applyExifRotation(bitmap, orientation)
        if (rotated != bitmap) bitmap.recycle()
        bitmap = rotated

        val baos = ByteArrayOutputStream()
        val ok = bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality.coerceIn(1, 100), baos)
        bitmap.recycle()
        if (!ok) error("JPEG 压缩失败")
        return baos.toByteArray()
    }

    private fun computeInSampleSize(longEdge: Int, maxLongEdge: Int): Int {
        if (maxLongEdge <= 0) return 1
        var sample = 1
        while (longEdge / sample > maxLongEdge) {
            sample *= 2
        }
        return sample.coerceAtLeast(1)
    }

    private fun applyExifRotation(bitmap: Bitmap, orientation: Int): Bitmap {
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        if (degrees == 0) return bitmap
        val m = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }
}
