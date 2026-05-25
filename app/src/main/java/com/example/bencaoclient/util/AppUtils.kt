package com.example.bencaoclient.util

import android.content.Context
import android.net.Uri
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun createImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = context.getExternalFilesDir("Pictures")
    val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun appGreenTopAppBarColors(): TopAppBarColors {
    val scheme = MaterialTheme.colorScheme
    return TopAppBarDefaults.topAppBarColors(
        containerColor = scheme.primary,
        titleContentColor = scheme.onPrimary,
        navigationIconContentColor = scheme.onPrimary,
        actionIconContentColor = scheme.onPrimary
    )
}
