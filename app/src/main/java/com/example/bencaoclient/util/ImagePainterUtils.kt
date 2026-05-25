package com.example.bencaoclient.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun rememberBencaoListThumbnailPainter(model: Any?): Painter {
    val context = LocalContext.current
    val density = LocalDensity.current
    val request = remember(model, density) {
        val w = with(density) { 240.dp.roundToPx() }.coerceAtLeast(1)
        val h = with(density) { 180.dp.roundToPx() }.coerceAtLeast(1)
        ImageRequest.Builder(context)
            .data(model)
            .size(w, h)
            .crossfade(false)
            .build()
    }
    return rememberAsyncImagePainter(request)
}

@Composable
fun rememberBencaoGalleryThumbPainter(model: Any?): Painter {
    val context = LocalContext.current
    val density = LocalDensity.current
    val request = remember(model, density) {
        val s = with(density) { 120.dp.roundToPx() }.coerceAtLeast(1)
        ImageRequest.Builder(context)
            .data(model)
            .size(s, s)
            .crossfade(false)
            .build()
    }
    return rememberAsyncImagePainter(request)
}

@Composable
fun rememberBencaoFullscreenPainter(model: Any?): Painter {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val request = remember(model, configuration.screenWidthDp, configuration.screenHeightDp, density) {
        val w = with(density) { configuration.screenWidthDp.dp.roundToPx() }.coerceAtLeast(1)
        val h = with(density) { configuration.screenHeightDp.dp.roundToPx() }.coerceAtLeast(1)
        ImageRequest.Builder(context)
            .data(model)
            .size(w, h)
            .crossfade(false)
            .build()
    }
    return rememberAsyncImagePainter(request)
}

@Composable
fun rememberHomeDiscoveryPainter(model: Any?): Painter {
    val context = LocalContext.current
    val density = LocalDensity.current
    val request = remember(model, density) {
        val w = with(density) { 360.dp.roundToPx() }.coerceAtLeast(1)
        val h = with(density) { 203.dp.roundToPx() }.coerceAtLeast(1)
        ImageRequest.Builder(context)
            .data(model)
            .size(w, h)
            .crossfade(false)
            .build()
    }
    return rememberAsyncImagePainter(request)
}
