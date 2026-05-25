package com.example.bencaoclient.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import com.example.bencaoclient.ui.theme.Dimens
import com.example.bencaoclient.util.decodeBitmapMaxSide
import com.example.bencaoclient.util.pickRandomBencaoShareSlogan
import com.example.bencaoclient.util.rememberBencaoFullscreenPainter
import com.example.bencaoclient.util.composeBencaoSharePoster
import com.example.bencaoclient.util.writePosterJpegForShare
import com.example.bencaoclient.util.launchShareImageChooser
import com.example.bencaoclient.util.saveOriginalImageFromUriToGallery
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun BencaoFullscreenZoomableImage(
    imageUri: String,
    onScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxW = constraints.maxWidth.toFloat()
        val maxH = constraints.maxHeight.toFloat()
        var scale by remember(imageUri) { mutableFloatStateOf(1f) }
        var offset by remember(imageUri) { mutableStateOf(Offset.Zero) }

        val transformState = rememberTransformableState { zoomChange, panChange, _ ->
            val newScale = (scale * zoomChange).coerceIn(1f, 5f)
            scale = newScale
            if (newScale <= 1f) {
                offset = Offset.Zero
            } else {
                val maxX = maxW * (newScale - 1f) / 2f
                val maxY = maxH * (newScale - 1f) / 2f
                offset = Offset(
                    x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
                    y = (offset.y + panChange.y).coerceIn(-maxY, maxY)
                )
            }
            onScaleChange(scale)
        }

        Image(
            painter = rememberBencaoFullscreenPainter(imageUri),
            contentDescription = "本草大图",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                    transformOrigin = TransformOrigin.Center
                )
                .transformable(state = transformState),
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BencaoImageFullscreenOverlay(
    imageUris: List<String>,
    initialPage: Int,
    bencaoName: String,
    createdAtMillis: Long,
    onDismiss: () -> Unit,
    pageBencaoNames: List<String>? = null,
    pageCreatedAtMillis: List<Long>? = null,
    onSharePosterSuccess: (() -> Unit)? = null,
) {
    if (imageUris.isEmpty()) return
    val start = initialPage.coerceIn(0, imageUris.lastIndex)
    val pagerState = rememberPagerState(
        initialPage = start,
        pageCount = { imageUris.size }
    )
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var posterSaving by remember { mutableStateOf(false) }
    var originalSaving by remember { mutableStateOf(false) }
    val busy = posterSaving || originalSaving
    val pageZoomScale = remember { mutableStateMapOf<Int, Float>() }
    val pagerScrollEnabled =
        (pageZoomScale[pagerState.currentPage] ?: 1f) <= 1.01f
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = pagerScrollEnabled
            ) { page ->
                BencaoFullscreenZoomableImage(
                    imageUri = imageUris[page],
                    onScaleChange = { s ->
                        if (s <= 1.01f) pageZoomScale.remove(page)
                        else pageZoomScale[page] = s
                    }
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        if (busy) return@TextButton
                        val page = pagerState.currentPage
                        val uriStr = imageUris.getOrNull(page)?.trim().orEmpty()
                        if (uriStr.isEmpty()) return@TextButton
                        posterSaving = true
                        scope.launch {
                            val shareName =
                                pageBencaoNames?.getOrNull(page)?.takeIf { it.isNotBlank() } ?: bencaoName
                            val shareCreatedAt =
                                pageCreatedAtMillis?.getOrNull(page) ?: createdAtMillis
                            val poster = runCatching {
                                withContext(Dispatchers.IO) {
                                    val photo = decodeBitmapMaxSide(context, uriStr, 2560)
                                        ?: return@withContext null
                                    try {
                                        val slogan = pickRandomBencaoShareSlogan()
                                        composeBencaoSharePoster(
                                            photo,
                                            shareName,
                                            slogan,
                                            shareCreatedAt
                                        )
                                    } finally {
                                        if (!photo.isRecycled) photo.recycle()
                                    }
                                }
                            }.getOrNull()
                            if (poster == null) {
                                Toast.makeText(context, "无法生成海报", Toast.LENGTH_SHORT).show()
                            } else {
                                val shareUri = withContext(Dispatchers.IO) {
                                    writePosterJpegForShare(context, poster)
                                }
                                val ok = if (shareUri != null) {
                                    runCatching {
                                        launchShareImageChooser(
                                            context,
                                            shareUri,
                                            "image/jpeg",
                                            "分享海报"
                                        )
                                        true
                                    }.getOrDefault(false)
                                } else {
                                    false
                                }
                                if (!ok) {
                                    Toast.makeText(context, "无法打开分享", Toast.LENGTH_SHORT).show()
                                } else {
                                    onSharePosterSuccess?.invoke()
                                }
                                if (!poster.isRecycled) poster.recycle()
                            }
                            posterSaving = false
                        }
                    },
                    enabled = !busy,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "分享",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                TextButton(
                    onClick = {
                        if (busy) return@TextButton
                        val page = pagerState.currentPage
                        val uriStr = imageUris.getOrNull(page) ?: return@TextButton
                        originalSaving = true
                        scope.launch {
                            val ok = runCatching {
                                withContext(Dispatchers.IO) {
                                    saveOriginalImageFromUriToGallery(context, uriStr)
                                }
                            }.getOrDefault(false)
                            Toast.makeText(
                                context,
                                if (ok) "已保存原图到相册「图片/繁草」" else "下载失败",
                                Toast.LENGTH_SHORT
                            ).show()
                            originalSaving = false
                        }
                    },
                    enabled = !busy,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "下载",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Clear,
                        contentDescription = "关闭",
                        modifier = Modifier.size(30.dp),
                        tint = Color.White
                    )
                }
            }
            if (imageUris.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${imageUris.size}",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = Dimens.lg)
                )
            }
        }
    }
}
