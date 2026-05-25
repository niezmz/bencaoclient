package com.example.bencaoclient.activity

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.bencaoclient.ui.theme.Dimens
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

private const val PosterCarouselIntervalMs = 5_000L
/** 虚拟页倍数：自动播放始终 currentPage+1，避免末页回卷时反向滑动。 */
private const val PosterInfiniteLoopFactor = 400

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeActivityPosterCarousel(
    posterUris: List<String>,
    modifier: Modifier = Modifier,
) {
    if (posterUris.isEmpty()) return

    val posterCount = posterUris.size
    if (posterCount == 1) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(Dimens.radiusSm)),
        ) {
            ActivityPosterPage(uri = posterUris[0])
        }
        return
    }

    key(posterUris) {
        val virtualCount = posterCount * PosterInfiniteLoopFactor
        val midBlock = posterCount * (PosterInfiniteLoopFactor / 2)
        val pagerState = rememberPagerState(
            initialPage = midBlock,
            pageCount = { virtualCount },
        )

        LaunchedEffect(posterUris) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                val band = posterCount * 20
                if (page < band || page >= virtualCount - band) {
                    val index = ((page % posterCount) + posterCount) % posterCount
                    pagerState.scrollToPage(midBlock + index)
                }
            }
        }

        LaunchedEffect(posterUris) {
            while (true) {
                delay(PosterCarouselIntervalMs)
                snapshotFlow { pagerState.isScrollInProgress }.first { !it }
                try {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                } catch (_: CancellationException) {
                    // 用户手势打断自动滑动；下一轮间隔后仍会尝试
                }
            }
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(Dimens.radiusSm)),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
            ) { page ->
                val index = ((page % posterCount) + posterCount) % posterCount
                ActivityPosterPage(uri = posterUris[index])
            }
        }
    }
}

@Composable
private fun ActivityPosterPage(uri: String) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val request = remember(uri, density) {
        val w = with(density) { 360.dp.roundToPx() }.coerceAtLeast(1)
        val h = with(density) { 203.dp.roundToPx() }.coerceAtLeast(1)
        ImageRequest.Builder(context)
            .data(uri)
            .size(w, h)
            .crossfade(true)
            .build()
    }
    Image(
        painter = rememberAsyncImagePainter(request),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
    )
}
