package com.example.bencaoclient.util

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

fun writePosterJpegForShare(context: Context, bitmap: Bitmap): Uri? {
    val dir = File(context.cacheDir, "share_posters").apply { mkdirs() }
    val file = File(dir, "poster_${System.currentTimeMillis()}.jpg")
    return try {
        file.outputStream().use { out ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)) {
                file.delete()
                return null
            }
        }
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    } catch (_: Throwable) {
        runCatching { file.delete() }
        null
    }
}

fun launchShareImageChooser(context: Context, uri: Uri, mimeType: String, chooserTitle: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newUri(context.contentResolver, "image", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(send, chooserTitle).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(chooser)
}

fun saveOriginalImageFromUriToGallery(context: Context, uriString: String): Boolean {
    val srcUri = normalizeImageUriForContentResolver(uriString) ?: return false
    val resolver = context.contentResolver
    val mime = resolver.getType(srcUri) ?: "image/jpeg"
    val ext = when {
        mime.equals("image/png", ignoreCase = true) -> "png"
        mime.equals("image/webp", ignoreCase = true) -> "webp"
        else -> "jpg"
    }
    val fileName = "繁草原图_${System.currentTimeMillis()}.$ext"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, mime)
        put(
            MediaStore.MediaColumns.RELATIVE_PATH,
            "${Environment.DIRECTORY_PICTURES}/繁草"
        )
        put(MediaStore.MediaColumns.IS_PENDING, 1)
    }
    val destUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        ?: return false
    return try {
        resolver.openInputStream(srcUri)?.use { input ->
            resolver.openOutputStream(destUri)?.use { output ->
                input.copyTo(output)
            } ?: run {
                resolver.delete(destUri, null, null)
                return false
            }
        } ?: run {
            resolver.delete(destUri, null, null)
            return false
        }
        contentValues.clear()
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(destUri, contentValues, null, null)
        true
    } catch (_: Throwable) {
        runCatching { resolver.delete(destUri, null, null) }
        false
    }
}
