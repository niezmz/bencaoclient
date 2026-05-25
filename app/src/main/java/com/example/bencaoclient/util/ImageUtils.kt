package com.example.bencaoclient.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import kotlin.math.max

/**
 * [Bencao.images] 中可能是 `content://`、导入后的裸绝对路径（`/data/.../species_media/...`）等。
 * 裸路径经 [Uri.parse] 后无有效 scheme，[ContentResolver] 无法打开，会导致海报/下载失败；存在且为文件时规范为 `file://`。
 */
fun normalizeImageUriForContentResolver(uriString: String): Uri? {
    val trimmed = uriString.trim()
    if (trimmed.isEmpty()) return null
    val parsed = runCatching { Uri.parse(trimmed) }.getOrNull() ?: return null
    val scheme = parsed.scheme?.takeIf { it.isNotEmpty() }
    if (scheme != null && !scheme.equals("file", ignoreCase = true)) {
        return parsed
    }
    val path = when {
        scheme.equals("file", ignoreCase = true) -> parsed.path
        trimmed.startsWith("/") -> trimmed
        else -> parsed.path
    }?.takeIf { it.startsWith("/") } ?: return parsed
    val f = File(path)
    return if (f.isFile) f.toUri() else parsed
}

fun decodeBitmapMaxSide(context: Context, uriString: String, maxSide: Int): Bitmap? {
    val uri = normalizeImageUriForContentResolver(uriString) ?: return null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        runCatching {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val w = info.size.width
                val h = info.size.height
                if (w > 0 && h > 0) {
                    val maxDim = max(w, h)
                    var sample = 1
                    while (maxDim / sample > maxSide) sample *= 2
                    val tw = (w / sample).coerceAtLeast(1)
                    val th = (h / sample).coerceAtLeast(1)
                    decoder.setTargetSize(tw, th)
                }
            }
        }.getOrNull()?.let { return it }
    }

    return decodeBitmapMaxSideStreamFallback(context, uri, maxSide)
}

private fun decodeBitmapMaxSideStreamFallback(context: Context, uri: Uri, maxSide: Int): Bitmap? {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        ?: return null
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    val maxDim = max(bounds.outWidth, bounds.outHeight)
    while (maxDim / sample > maxSide) sample *= 2
    val opts = BitmapFactory.Options().apply {
        inSampleSize = sample
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        ?: return null

    val orientation = runCatching {
        resolver.openInputStream(uri)?.use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
        }
    }.getOrNull() ?: ExifInterface.ORIENTATION_UNDEFINED

    return applyExifRotationIfNeeded(decoded, orientation)
}

fun applyExifRotationIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
    val degrees = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
    if (degrees == 0) return bitmap
    val m = Matrix().apply { postRotate(degrees.toFloat()) }
    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    if (rotated != bitmap) bitmap.recycle()
    return rotated
}
