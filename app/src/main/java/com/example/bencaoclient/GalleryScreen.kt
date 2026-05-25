package com.example.bencaoclient

import androidx.compose.foundation.background
import com.example.bencaoclient.ui.component.BencaoImageFullscreenOverlay
import com.example.bencaoclient.ui.component.BencaoFullscreenZoomableImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.bencaoclient.ui.theme.Dimens
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.roundToInt

private data class GalleryGridItem(
    val bencaoId: Long,
    val imageIndex: Int,
    val uri: String,
    val stableKey: String,
    val bencaoName: String,
    val createdAtMillis: Long
)

private data class GalleryOverlaySnapshot(
    val items: List<GalleryGridItem>,
    val startIndex: Int
)

private const val SPECIES_PAGE_SIZE = 14

@Composable
fun GalleryScreen(onSharePosterSuccess: (() -> Unit)? = null) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val decodeWidthPx = remember(screenWidthDp, density) {
        with(density) {
            (screenWidthDp / 3).dp.toPx().roundToInt().coerceAtLeast(64)
        }
    }
    val decodeSize = remember(decodeWidthPx) {
        Size(decodeWidthPx, decodeWidthPx)
    }

    val gridState = rememberLazyGridState()
    val cells = remember { mutableStateListOf<GalleryGridItem>() }
    var speciesDbOffset by remember { mutableIntStateOf(0) }
    var hasMoreSpecies by remember { mutableStateOf(true) }
    var initialLoading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    val fetchMutex = remember { Mutex() }
    var overlay by remember { mutableStateOf<GalleryOverlaySnapshot?>(null) }

    suspend fun fetchOneSpeciesBatch() {
        fetchMutex.withLock {
            if (!hasMoreSpecies) return@withLock
            loadingMore = true
            try {
                val batch = BencaoRepository.getGallerySpeciesSlice(
                    offset = speciesDbOffset,
                    limit = SPECIES_PAGE_SIZE
                )
                if (batch.isEmpty()) {
                    hasMoreSpecies = false
                    return@withLock
                }
                speciesDbOffset += batch.size
                if (batch.size < SPECIES_PAGE_SIZE) {
                    hasMoreSpecies = false
                }
                for (b in batch) {
                    b.images.forEachIndexed { idx, uri ->
                        if (uri.isNotBlank()) {
                            cells.add(
                                GalleryGridItem(
                                    bencaoId = b.id,
                                    imageIndex = idx,
                                    uri = uri,
                                    stableKey = "${b.id}_$idx",
                                    bencaoName = b.name,
                                    createdAtMillis = b.createdAt.time
                                )
                            )
                        }
                    }
                }
            } finally {
                loadingMore = false
            }
        }
    }

    LaunchedEffect(Unit) {
        while (hasMoreSpecies && cells.size < 12) {
            val sizeBefore = cells.size
            fetchOneSpeciesBatch()
            if (!hasMoreSpecies) break
            if (cells.size > sizeBefore) break
        }
        initialLoading = false
    }

    LaunchedEffect(gridState) {
        snapshotFlow {
            Triple(
                gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1,
                cells.size,
                hasMoreSpecies to initialLoading
            )
        }.collect { (lastVisible, imageCellCount, flags) ->
            val (more, init) = flags
            if (init || !more || imageCellCount <= 0) return@collect
            if (lastVisible >= imageCellCount - 15) {
                while (hasMoreSpecies) {
                    val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: break
                    val c = cells.size
                    if (last < c - 15) break
                    val before = cells.size
                    fetchOneSpeciesBatch()
                    if (cells.size == before) break
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .heightIn(min = 56.dp)
                        .padding(vertical = Dimens.sm),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "画廊",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            when {
                initialLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                cells.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无图片",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = gridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(Dimens.xs),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.xs),
                        verticalArrangement = Arrangement.spacedBy(Dimens.xs)
                    ) {
                        items(
                            count = cells.size,
                            key = { index -> cells[index].stableKey }
                        ) { index ->
                            val item = cells[index]
                            val model = remember(item.uri, decodeSize) {
                                ImageRequest.Builder(context)
                                    .data(item.uri)
                                    .size(decodeSize)
                                    .crossfade(false)
                                    .build()
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = model,
                                    contentDescription = item.bencaoName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(Dimens.radiusSm))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable(
                                            interactionSource = remember(item.stableKey) {
                                                MutableInteractionSource()
                                            },
                                            indication = null
                                        ) {
                                            overlay = GalleryOverlaySnapshot(
                                                items = cells.toList(),
                                                startIndex = index
                                            )
                                        },
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = item.bencaoName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp, start = 2.dp, end = 2.dp)
                                )
                            }
                        }
                        if (hasMoreSpecies || loadingMore) {
                            item(span = { GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Dimens.lg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (loadingMore) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

            overlay?.let { snap ->
                if (snap.items.isNotEmpty()) {
                    val start = snap.startIndex.coerceIn(0, snap.items.lastIndex)
                    val first = snap.items[start]
                    BencaoImageFullscreenOverlay(
                        imageUris = snap.items.map { it.uri },
                        initialPage = start,
                        bencaoName = first.bencaoName,
                        createdAtMillis = first.createdAtMillis,
                        pageBencaoNames = snap.items.map { it.bencaoName },
                        pageCreatedAtMillis = snap.items.map { it.createdAtMillis },
                        onDismiss = { overlay = null },
                        onSharePosterSuccess = onSharePosterSuccess,
                    )
                }
            }
        }
    }
}
