package com.example.bencaoclient

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star

import androidx.compose.material.icons.outlined.Category

import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.AccountBalanceWallet

import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PestControl
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Yard
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.em
import com.example.bencaoclient.ui.theme.BencaoGreen
import com.example.bencaoclient.ui.theme.BencaoGreenOnContainer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.example.bencaoclient.ai.DoubaoAi
import com.example.bencaoclient.activity.ActivityPosterResolver
import com.example.bencaoclient.activity.HomeActivityPosterCarousel
import com.example.bencaoclient.model.*
import com.example.bencaoclient.ui.component.*
import com.example.bencaoclient.ui.screen.*
import com.example.bencaoclient.util.*
import com.example.bencaoclient.ui.theme.BencaoBrandAccent
import com.example.bencaoclient.ui.theme.BencaoGreen
import com.example.bencaoclient.ui.theme.BencaoGreenOnContainer
import com.example.bencaoclient.ui.theme.BencaoclientTheme
import com.example.bencaoclient.ui.theme.Dimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        BencaoRepository.init(applicationContext)

        setContent {
            BencaoclientTheme {
                MainScreen()
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen() {
    var showAddBencao by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var backupOverlayUi by remember { mutableStateOf<SpeciesBackupOverlayUi?>(null) }
    val backupBlocking = backupOverlayUi != null

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    LaunchedEffect(Unit) {
        DoubaoAi.setActiveApiKey(ApiKeyStore.getActiveKey(context))
    }

    // 相机启动器
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            showAddBencao = true
        } else {
            // 用户取消/未拍照返回时，停留在“新增”页，不要跳回“资料库”
            capturedImageUri = null
            showAddBencao = false
            navController.navigate("discover") {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    
    // 相机权限请求
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createImageUri(context)
            capturedImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val launchShootIdentify: () -> Unit = {
        if (!backupBlocking) {
            navController.navigate("discover") {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                    val uri = createImageUri(context)
                    capturedImageUri = uri
                    cameraLauncher.launch(uri)
                }
                else -> {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    // 登录功能已移除（单机版）
    if (showAddBencao && capturedImageUri != null) {
        AddBencaoScreen(
            imageUri = capturedImageUri!!,
            onSave = { bencao ->
                scope.launch {
                    BencaoRepository.addBencao(bencao)
                    showAddBencao = false
                    capturedImageUri = null
                    navController.navigate("library") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            onCancel = {
                showAddBencao = false
                capturedImageUri = null
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            // MainScreen 是 NavHost 的父容器，本身没有 topBar。
            // Scaffold 默认会把 safeDrawing/systemBars 的 top inset 体现在 innerPadding 里；
            // 而各子页面又有 TopAppBar（会消费 status bar/cutout inset），会造成“顶部安全区”叠加变大。
            // 新增本草不在这个 Scaffold 里渲染，所以它看起来正常；其它列表/详情/搜索/同科列表会被叠加推下去。
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                val navItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                NavigationBar {
                    val items = listOf(
                        NavigationItem("首页", 0),
                        NavigationItem("分类", 1),
                        NavigationItem("拍摄识图", 2),
                        NavigationItem("画廊", 3),
                        NavigationItem("我的", 4)
                    )
                    items.forEach { item ->
                        NavigationBarItem(
                            colors = navItemColors,
                            icon = {
                                when (item.id) {
                                    0 -> Icon(imageVector = Icons.Outlined.Home, contentDescription = item.name)
                                    1 -> Icon(imageVector = Icons.Outlined.Category, contentDescription = item.name)
                                    2 -> Icon(imageVector = Icons.Outlined.PhotoCamera, contentDescription = item.name)
                                    3 -> Icon(imageVector = Icons.Outlined.PhotoLibrary, contentDescription = item.name)
                                    4 -> Icon(imageVector = Icons.Outlined.Person, contentDescription = item.name)
                                    else -> Icon(imageVector = Icons.Outlined.Home, contentDescription = item.name)
                                }
                            },
                            label = { Text(item.name) },
                            selected = when (item.id) {
                                0 -> currentRoute == "home"
                                1 -> currentRoute == "category"
                                2 -> currentRoute == "discover"
                                3 -> currentRoute == "gallery"
                                4 -> currentRoute == "mine"
                                else -> false
                            },
                            onClick = navClick@{
                                if (backupBlocking) return@navClick
                                if (item.id == 2) {
                                    launchShootIdentify()
                                } else {
                                    val route = when (item.id) {
                                        0 -> "home"
                                        1 -> "category"
                                        3 -> "gallery"
                                        4 -> "mine"
                                        else -> "home"
                                    }
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    HomeScreen(
                        backupBlocking = backupBlocking,
                        onShootIdentify = launchShootIdentify,
                        onOpenSpeciesLibrary = {
                            if (!backupBlocking) {
                                navController.navigate("library") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        onOpenPlantingMethods = {
                            if (!backupBlocking) {
                                navController.navigate("planting_methods") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        onOpenAiSettings = {
                            if (!backupBlocking) {
                                navController.navigate("ai_settings") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        onOpenBencaoDetail = { id ->
                            if (!backupBlocking) {
                                navController.navigate("bencao_detail/$id")
                            }
                        }
                    )
                }
                composable("library") {
                    LibraryScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("planting_methods") {
                    PlantingMethodsLibraryScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToSpeciesDetail = { id ->
                            navController.navigate("bencao_detail/$id")
                        }
                    )
                }
                composable("ai_settings") {
                    AiSettingsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("gallery") {
                    GalleryScreen()
                }
                composable("discover") { AddScreen() }
                composable("mine") {
                    MineScreen(
                        onBackupOverlayChange = { backupOverlayUi = it }
                    )
                }
                composable("category") {
                    CategoryScreen(
                        onFamilyClick = { familyKey ->
                            navController.navigate("family/${Uri.encode(familyKey)}")
                        }
                    )
                }
                composable(
                    route = "family/{family}",
                    arguments = listOf(navArgument("family") { type = NavType.StringType })
                ) { entry ->
                    val familyKey = entry.arguments?.getString("family")?.let { Uri.decode(it) }.orEmpty()
                    FamilyLibraryScreen(
                        familyKey = familyKey,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "bencao_detail/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { entry ->
                    val id = entry.arguments?.getLong("id") ?: 0L
                    BencaoDetailByIdScreen(
                        bencaoId = id,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
        backupOverlayUi?.let { ui ->
            SpeciesBackupBlockingOverlay(ui = ui)
        }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CategoryScreen(
    onFamilyClick: (String) -> Unit
) {
    val bencaos by BencaoRepository
        .observeAllBencaos()
        .collectAsState(initial = emptyList())

    val familyStats = remember(bencaos) {
        val normalizedFamilies = bencaos.map { b ->
            b.family.trim().ifBlank { "未填写" }
        }
        val familyToCount = normalizedFamilies
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })

        // 区域2背景图：每个 family 取“第一个 Bencao 的第一张图”
        val familyToFirstImage = buildMap<String, String> {
            for (b in bencaos) {
                val family = b.family.trim().ifBlank { "未填写" }
                if (containsKey(family)) continue
                val first = b.images.firstOrNull()?.trim().orEmpty()
                if (first.isNotEmpty()) {
                    put(family, first)
                }
            }
        }
        familyToCount to familyToFirstImage
    }

    val familyToCount = familyStats.first
    val familyToFirstImage = familyStats.second

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "分类") },
                colors = appGreenTopAppBarColors()
            )
        }
        ,
        // TopAppBar 已经消费了 status bar/cutout inset；Scaffold 默认 contentWindowInsets
        // 会再次把 system bars inset 加到内容区，导致不同页面顶部“安全区”看起来不一致且偏大。
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.lg)
        ) {
            if (familyToCount.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无分类",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Dimens.lg),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                items(familyToCount, key = { it.first }) { (family, count) ->
                    val shape = RoundedCornerShape(Dimens.radiusMd)
                    val bg = familyToFirstImage[family]
                    val scheme = MaterialTheme.colorScheme
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth()
                            .clickable { onFamilyClick(family) },
                        shape = shape,
                        colors = CardDefaults.cardColors(
                            containerColor = scheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 1.dp,
                                    color = scheme.primary.copy(alpha = 0.28f),
                                    shape = shape
                                )
                                .clip(shape)
                        ) {
                            if (!bg.isNullOrBlank()) {
                                Image(
                                    painter = rememberBencaoListThumbnailPainter(bg),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // 轻微遮罩，保证文字可读
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f))
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(Dimens.sm),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = family,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    color = if (!bg.isNullOrBlank()) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(Dimens.xs))
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (!bg.isNullOrBlank()) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            } // end else (familyToCount non-empty)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
private fun FamilyLibraryScreen(
    familyKey: String,
    onBack: () -> Unit,
    onSharePosterSuccess: (() -> Unit)? = null,
) {
    var currentPage by remember { mutableIntStateOf(0) }
    var sortMode by remember { mutableStateOf(LibrarySortMode.CreatedAtDesc) }
    var detailBencao by remember { mutableStateOf<Bencao?>(null) }
    var bencaoToDelete by remember { mutableStateOf<Bencao?>(null) }
    var appendPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var appendPhotoBencaoId by remember { mutableStateOf<Long?>(null) }
    /** 全屏看图 */
    var fullscreenImages by remember { mutableStateOf<BencaoFullscreenSession?>(null) }

    val pageSize = 10
    val scope = rememberCoroutineScope()
    val familyContext = LocalContext.current

    val appendCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = appendPhotoUri
        val bencaoId = appendPhotoBencaoId
        appendPhotoUri = null
        appendPhotoBencaoId = null
        if (success && uri != null && bencaoId != null) {
            scope.launch {
                val current = BencaoRepository.getBencaoById(bencaoId)
                if (current != null && current.images.size < 10) {
                    BencaoRepository.updateBencao(
                        current.copy(images = current.images + uri.toString())
                    )
                }
            }
        }
    }

    val appendCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val id = appendPhotoBencaoId
            if (id != null) {
                val uri = createImageUri(familyContext)
                appendPhotoUri = uri
                appendCameraLauncher.launch(uri)
            }
        } else {
            appendPhotoBencaoId = null
        }
    }

    fun startAppendPhoto(bencaoId: Long) {
        appendPhotoBencaoId = bencaoId
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(familyContext, Manifest.permission.CAMERA) -> {
                val uri = createImageUri(familyContext)
                appendPhotoUri = uri
                appendCameraLauncher.launch(uri)
            }
            else -> {
                appendCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    val listFlow = remember(familyKey, sortMode, currentPage, pageSize) {
        BencaoRepository.observeFamilyPage(familyKey, sortMode, currentPage, pageSize)
    }
    val bencaos by listFlow.collectAsState(initial = emptyList())

    val totalPages by BencaoRepository
        .observeFamilyTotalPages(familyKey, pageSize)
        .collectAsState(initial = 1)

    LaunchedEffect(totalPages, currentPage) {
        if (currentPage >= totalPages) {
            currentPage = maxOf(0, totalPages - 1)
        }
    }

    if (detailBencao != null) {
        val initialId = detailBencao!!.id
        val familyIdsFlow = remember(familyKey, sortMode) {
            BencaoRepository.observeFamilyBencaoIdsSorted(familyKey, sortMode)
        }
        val familyIds by familyIdsFlow.collectAsState(initial = emptyList())
        val pageCount = familyIds.size

        if (familyIds.isEmpty()) {
            val detailFlow = remember(initialId) { BencaoRepository.observeBencaoById(initialId) }
            val detailFromDb by detailFlow.collectAsState(initial = detailBencao!!)
            val detailModel = detailFromDb ?: detailBencao!!
            BencaoDetailScreen(
                bencao = detailModel,
                onBack = { detailBencao = null },
                onAppendPhotoClick = { startAppendPhoto(detailModel.id) },
                appendPhotoEnabled = detailModel.images.size < 10,
                onUpdateBencao = { updated ->
                    scope.launch { BencaoRepository.updateBencao(updated) }
                },
                onImageClick = { uris, index, name, createdAt ->
                    fullscreenImages = BencaoFullscreenSession(uris, index, name, createdAt)
                },
                onDeletePhoto = { index ->
                    scope.launch {
                        val current = BencaoRepository.getBencaoById(detailModel.id) ?: return@launch
                        if (current.images.size < 2) return@launch
                        if (index !in current.images.indices) return@launch
                        val newImages = current.images.toMutableList().apply { removeAt(index) }
                        if (newImages.isEmpty()) return@launch
                        BencaoRepository.updateBencao(current.copy(images = newImages))
                    }
                }
            )
        } else if (pageCount <= 1) {
            val detailFlow = remember(initialId) { BencaoRepository.observeBencaoById(initialId) }
            val detailFromDb by detailFlow.collectAsState(initial = detailBencao!!)
            val detailModel = detailFromDb ?: detailBencao!!
            BencaoDetailScreen(
                bencao = detailModel,
                onBack = { detailBencao = null },
                onAppendPhotoClick = { startAppendPhoto(detailModel.id) },
                appendPhotoEnabled = detailModel.images.size < 10,
                onUpdateBencao = { updated ->
                    scope.launch { BencaoRepository.updateBencao(updated) }
                },
                onImageClick = { uris, index, name, createdAt ->
                    fullscreenImages = BencaoFullscreenSession(uris, index, name, createdAt)
                },
                onDeletePhoto = { index ->
                    scope.launch {
                        val current = BencaoRepository.getBencaoById(detailModel.id) ?: return@launch
                        if (current.images.size < 2) return@launch
                        if (index !in current.images.indices) return@launch
                        val newImages = current.images.toMutableList().apply { removeAt(index) }
                        if (newImages.isEmpty()) return@launch
                        BencaoRepository.updateBencao(current.copy(images = newImages))
                    }
                }
            )
        } else {
            val initialPage = remember(familyIds, initialId) { familyIds.indexOf(initialId).coerceAtLeast(0) }
            val pagerState = rememberPagerState(
                initialPage = initialPage,
                pageCount = { pageCount }
            )

            LaunchedEffect(familyIds, initialId) {
                val target = familyIds.indexOf(initialId)
                if (target >= 0 && target != pagerState.currentPage) {
                    pagerState.scrollToPage(target)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageId = familyIds.getOrNull(page) ?: return@HorizontalPager
                val flow = remember(pageId) { BencaoRepository.observeBencaoById(pageId) }
                val initialForPage = remember(pageId, initialId) {
                    if (pageId == initialId) detailBencao else null
                }
                val modelFromDb by flow.collectAsState(initial = initialForPage)
                val model = modelFromDb
                if (model != null) {
                    BencaoDetailScreen(
                        bencao = model,
                        onBack = { detailBencao = null },
                        onAppendPhotoClick = { startAppendPhoto(model.id) },
                        appendPhotoEnabled = model.images.size < 10,
                        onUpdateBencao = { updated ->
                            scope.launch { BencaoRepository.updateBencao(updated) }
                        },
                        onImageClick = { uris, index, name, createdAt ->
                            fullscreenImages = BencaoFullscreenSession(uris, index, name, createdAt)
                        },
                        onDeletePhoto = { index ->
                            scope.launch {
                                val current = BencaoRepository.getBencaoById(model.id) ?: return@launch
                                if (current.images.size < 2) return@launch
                                if (index !in current.images.indices) return@launch
                                val newImages = current.images.toMutableList().apply { removeAt(index) }
                                if (newImages.isEmpty()) return@launch
                                BencaoRepository.updateBencao(current.copy(images = newImages))
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = familyKey) },
                    colors = appGreenTopAppBarColors(),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    },
                    actions = {
                        Text(
                            text = sortMode.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
                        )
                        IconButton(
                            onClick = {
                                sortMode = sortMode.next()
                                currentPage = 0
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SwapVert,
                                contentDescription = "切换排序"
                            )
                        }
                    }
                )
            }
            ,
            // 避免 Scaffold 默认把 status bar inset 再加到内容区（TopAppBar 已处理）
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (bencaos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "暂无数据")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(Dimens.lg),
                        verticalArrangement = Arrangement.spacedBy(Dimens.lg)
                    ) {
                        items(bencaos, key = { it.id }) { bencao ->
                            BencaoCard(
                                bencao = bencao,
                                onClick = { detailBencao = bencao },
                                onDeleteClick = { bencaoToDelete = bencao },
                                onImageClick = { uris, index, name, createdAt ->
                fullscreenImages = BencaoFullscreenSession(uris, index, name, createdAt)
            }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.lg),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { if (currentPage > 0) currentPage-- },
                            enabled = currentPage > 0
                        ) {
                            Text("上一页")
                        }

                        Text(
                            text = "${currentPage + 1} / $totalPages",
                            modifier = Modifier.padding(horizontal = Dimens.lg)
                        )

                        OutlinedButton(
                            onClick = { if (currentPage < totalPages - 1) currentPage++ },
                            enabled = currentPage < totalPages - 1
                        ) {
                            Text("下一页")
                        }
                    }
                }
            }
        }
    }

    bencaoToDelete?.let { b ->
        AlertDialog(
            onDismissRequest = { bencaoToDelete = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${b.name}」吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = b.id
                        bencaoToDelete = null
                        scope.launch {
                            BencaoRepository.deleteBencaoById(id)
                        }
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { bencaoToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    fullscreenImages?.let { session ->
        BencaoImageFullscreenOverlay(
            imageUris = session.imageUris,
            initialPage = session.initialPage,
            bencaoName = session.bencaoName,
            createdAtMillis = session.createdAtMillis,
            onDismiss = { fullscreenImages = null },
            onSharePosterSuccess = onSharePosterSuccess,
        )
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LibraryScreen(
    onBack: () -> Unit = {},
    onSharePosterSuccess: (() -> Unit)? = null,
) {
    var currentPage by remember { mutableIntStateOf(0) }
    var sortMode by remember { mutableStateOf(LibrarySortMode.CreatedAtDesc) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Bencao>?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var detailBencao by remember { mutableStateOf<Bencao?>(null) }
    var bencaoToDelete by remember { mutableStateOf<Bencao?>(null) }
    var appendPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var appendPhotoBencaoId by remember { mutableStateOf<Long?>(null) }
    /** 全屏看图 */
    var fullscreenImages by remember { mutableStateOf<BencaoFullscreenSession?>(null) }
    val pageSize = 10
    val scope = rememberCoroutineScope()
    val libraryContext = LocalContext.current

    val libraryFlow = remember(sortMode, currentPage, pageSize) {
        BencaoRepository.observeLibraryPage(sortMode, currentPage, pageSize)
    }
    val bencaos by libraryFlow.collectAsState(initial = emptyList())

    val appendCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = appendPhotoUri
        val bencaoId = appendPhotoBencaoId
        appendPhotoUri = null
        appendPhotoBencaoId = null
        if (success && uri != null && bencaoId != null) {
            scope.launch {
                val current = BencaoRepository.getBencaoById(bencaoId)
                if (current != null && current.images.size < 10) {
                    BencaoRepository.updateBencao(
                        current.copy(images = current.images + uri.toString())
                    )
                }
            }
        }
    }

    val appendCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val id = appendPhotoBencaoId
            if (id != null) {
                val uri = createImageUri(libraryContext)
                appendPhotoUri = uri
                appendCameraLauncher.launch(uri)
            }
        } else {
            appendPhotoBencaoId = null
        }
    }

    fun startAppendPhoto(bencaoId: Long) {
        appendPhotoBencaoId = bencaoId
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(libraryContext, Manifest.permission.CAMERA) -> {
                val uri = createImageUri(libraryContext)
                appendPhotoUri = uri
                appendCameraLauncher.launch(uri)
            }
            else -> {
                appendCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    val totalPages by BencaoRepository
        .observeTotalPages(pageSize)
        .collectAsState(initial = 1)

    LaunchedEffect(totalPages, currentPage) {
        if (currentPage >= totalPages) {
            currentPage = maxOf(0, totalPages - 1)
        }
    }

    if (detailBencao != null) {
        val initialId = detailBencao!!.id
        val allIdsFlow = remember(sortMode) { BencaoRepository.observeAllBencaoIdsSorted(sortMode) }
        val allIds by allIdsFlow.collectAsState(initial = emptyList())
        val pageCount = allIds.size

        // ids 还没加载出来时，先直接渲染单条详情，避免“先落到第 0 页再跳页”的闪烁
        if (allIds.isEmpty()) {
            val detailFlow = remember(initialId) { BencaoRepository.observeBencaoById(initialId) }
            val detailFromDb by detailFlow.collectAsState(initial = detailBencao!!)
            val detailModel = detailFromDb ?: detailBencao!!
            BencaoDetailScreen(
                bencao = detailModel,
                onBack = { detailBencao = null },
                onAppendPhotoClick = { startAppendPhoto(detailModel.id) },
                appendPhotoEnabled = detailModel.images.size < 10,
                onUpdateBencao = { updated ->
                    scope.launch { BencaoRepository.updateBencao(updated) }
                },
                onImageClick = { uris, index, name, createdAt ->
                fullscreenImages = BencaoFullscreenSession(uris, index, name, createdAt)
            },
                onDeletePhoto = { index ->
                    scope.launch {
                        val current = BencaoRepository.getBencaoById(detailModel.id) ?: return@launch
                        if (current.images.size < 2) return@launch
                        if (index !in current.images.indices) return@launch
                        val newImages = current.images.toMutableList().apply { removeAt(index) }
                        if (newImages.isEmpty()) return@launch
                        BencaoRepository.updateBencao(current.copy(images = newImages))
                    }
                }
            )
        } else if (pageCount <= 1) {
            // 只有 1 条时：退化为单条详情（不需要左右滑）
            val detailFlow = remember(initialId) { BencaoRepository.observeBencaoById(initialId) }
            val detailFromDb by detailFlow.collectAsState(initial = detailBencao!!)
            val detailModel = detailFromDb ?: detailBencao!!
            BencaoDetailScreen(
                bencao = detailModel,
                onBack = { detailBencao = null },
                onAppendPhotoClick = { startAppendPhoto(detailModel.id) },
                appendPhotoEnabled = detailModel.images.size < 10,
                onUpdateBencao = { updated ->
                    scope.launch { BencaoRepository.updateBencao(updated) }
                },
                onImageClick = { uris, index, name, createdAt ->
                fullscreenImages = BencaoFullscreenSession(uris, index, name, createdAt)
            },
                onDeletePhoto = { index ->
                    scope.launch {
                        val current = BencaoRepository.getBencaoById(detailModel.id) ?: return@launch
                        if (current.images.size < 2) return@launch
                        if (index !in current.images.indices) return@launch
                        val newImages = current.images.toMutableList().apply { removeAt(index) }
                        if (newImages.isEmpty()) return@launch
                        BencaoRepository.updateBencao(current.copy(images = newImages))
                    }
                }
            )
        } else {
            // 首次进入详情时，把 pager 定位到当前条目；若排序/数据变化导致下标变化，也尽量跟随当前 id
            val initialPage = remember(allIds, initialId) { allIds.indexOf(initialId).coerceAtLeast(0) }
            val pagerState = rememberPagerState(
                initialPage = initialPage,
                pageCount = { pageCount }
            )

            LaunchedEffect(allIds, initialId) {
                val target = allIds.indexOf(initialId)
                if (target >= 0 && target != pagerState.currentPage) {
                    pagerState.scrollToPage(target)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageId = allIds.getOrNull(page) ?: return@HorizontalPager
                val flow = remember(pageId) { BencaoRepository.observeBencaoById(pageId) }
                // 初次进入时：当前页优先用点击那一条的 model，避免首帧 loading 闪一下
                val initialForPage = remember(pageId, initialId) {
                    if (pageId == initialId) detailBencao else null
                }
                val modelFromDb by flow.collectAsState(initial = initialForPage)
                val model = modelFromDb
                if (model != null) {
                    BencaoDetailScreen(
                        bencao = model,
                        onBack = { detailBencao = null },
                        onAppendPhotoClick = { startAppendPhoto(model.id) },
                        appendPhotoEnabled = model.images.size < 10,
                        onUpdateBencao = { updated ->
                            scope.launch { BencaoRepository.updateBencao(updated) }
                        },
                        onImageClick = { uris, index, name, createdAt ->
                fullscreenImages = BencaoFullscreenSession(uris, index, name, createdAt)
            },
                        onDeletePhoto = { index ->
                            scope.launch {
                                val current = BencaoRepository.getBencaoById(model.id) ?: return@launch
                                if (current.images.size < 2) return@launch
                                if (index !in current.images.indices) return@launch
                                val newImages = current.images.toMutableList().apply { removeAt(index) }
                                if (newImages.isEmpty()) return@launch
                                BencaoRepository.updateBencao(current.copy(images = newImages))
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    } else if (searchResults != null) {
        LibrarySearchResultsScreen(
            searchQuery = searchQuery,
            results = searchResults!!,
            onBack = { searchResults = null },
            onBencaoClick = { detailBencao = it },
            onDeleteRequest = { bencaoToDelete = it },
            onImageClick = { uris, index, name, createdAt ->
                fullscreenImages = BencaoFullscreenSession(uris, index, name, createdAt)
            }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .heightIn(min = 56.dp)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "返回",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        IconButton(onClick = { showSearchDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "搜索",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Text(
                        text = "已发现物种",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sortMode.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
                        )
                        Spacer(modifier = Modifier.width(Dimens.sm))
                        IconButton(
                            onClick = {
                                sortMode = sortMode.next()
                                currentPage = 0
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SwapVert,
                                contentDescription = "切换排序",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            if (bencaos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "暂无数据")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(Dimens.lg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.lg)
                ) {
                    items(bencaos, key = { it.id }) { bencao ->
                        BencaoCard(
                            bencao = bencao,
                            onClick = { detailBencao = bencao },
                            onDeleteClick = { bencaoToDelete = bencao },
                            onImageClick = { uris, index, name, createdAt ->
                fullscreenImages = BencaoFullscreenSession(uris, index, name, createdAt)
            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.lg),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentPage > 0) currentPage-- },
                        enabled = currentPage > 0
                    ) {
                        Text("上一页")
                    }

                    Text(
                        text = "${currentPage + 1} / $totalPages",
                        modifier = Modifier.padding(horizontal = Dimens.lg)
                    )

                    OutlinedButton(
                        onClick = { if (currentPage < totalPages - 1) currentPage++ },
                        enabled = currentPage < totalPages - 1
                    ) {
                        Text("下一页")
                    }
                }
            }
        }
    }

    if (showSearchDialog) {
        LibrarySearchDialog(
            onDismiss = { showSearchDialog = false },
            onSubmitSearch = { q ->
                val trimmed = q.trim()
                searchQuery = trimmed
                showSearchDialog = false
                scope.launch {
                    searchResults =
                        if (trimmed.isEmpty()) emptyList()
                        else rankBencaosByNameRelevance(
                            BencaoRepository.getAllBencaosSnapshot(),
                            trimmed
                        ).take(10)
                }
            }
        )
    }

    bencaoToDelete?.let { b ->
        AlertDialog(
            onDismissRequest = { bencaoToDelete = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${b.name}」吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = b.id
                        val snapshot = searchResults
                        bencaoToDelete = null
                        scope.launch {
                            BencaoRepository.deleteBencaoById(id)
                            if (snapshot != null) {
                                searchResults = snapshot.filter { it.id != id }
                            }
                        }
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { bencaoToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    fullscreenImages?.let { session ->
        BencaoImageFullscreenOverlay(
            imageUris = session.imageUris,
            initialPage = session.initialPage,
            bencaoName = session.bencaoName,
            createdAtMillis = session.createdAtMillis,
            onDismiss = { fullscreenImages = null },
            onSharePosterSuccess = onSharePosterSuccess,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LibrarySearchResultsScreen(
    searchQuery: String,
    results: List<Bencao>,
    onBack: () -> Unit,
    onBencaoClick: (Bencao) -> Unit,
    onDeleteRequest: (Bencao) -> Unit,
    onImageClick: (List<String>, Int, String, Long) -> Unit
) {
    val emptyMessage = if (searchQuery.isBlank()) {
        "请输入关键字"
    } else {
        "无匹配结果"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text(text = "搜索结果") },
            colors = appGreenTopAppBarColors(),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        )
        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyMessage,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.lg)
            ) {
                items(results, key = { it.id }) { bencao ->
                    BencaoCard(
                        bencao = bencao,
                        onClick = { onBencaoClick(bencao) },
                        onDeleteClick = { onDeleteRequest(bencao) },
                        onImageClick = onImageClick
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LibrarySearchDialog(
    onDismiss: () -> Unit,
    onSubmitSearch: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    fun submit() {
        onSubmitSearch(query.trim())
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.radiusMd),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(Dimens.lg)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "按名称搜索",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = "关闭"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { new ->
                            query = trimToMaxCodePoints(new, LibrarySearchMaxNameQueryCodePoints)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp),
                        label = { Text("名称关键字") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { submit() })
                    )
                    Button(
                        onClick = { submit() },
                        modifier = Modifier.heightIn(min = 56.dp)
                    ) {
                        Text("搜索")
                    }
                }
                Text(
                    text = "最多 6 个汉字",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimens.xs)
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BencaoDetailScreen(
    bencao: Bencao,
    onBack: () -> Unit,
    onAppendPhotoClick: () -> Unit = {},
    appendPhotoEnabled: Boolean = true,
    onImageClick: (List<String>, Int, String, Long) -> Unit = { _, _, _, _ -> },
    onDeletePhoto: (index: Int) -> Unit = {},
    onUpdateBencao: (Bencao) -> Unit = {},
) {
    val createdAtText = remember(bencao.createdAt.time) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(bencao.createdAt)
    }

    var pendingDeleteImageIndex by remember(bencao.id, bencao.images.size) { mutableStateOf<Int?>(null) }
    var showEditDialog by rememberSaveable(bencao.id) { mutableStateOf(false) }
    var showPlantingDialog by rememberSaveable(bencao.id) { mutableStateOf(false) }
    var plantingDraft by remember(bencao.id) { mutableStateOf("") }
    var plantingFetchLoading by remember(bencao.id) { mutableStateOf(false) }
    var plantingFetchError by remember(bencao.id) { mutableStateOf<String?>(null) }
    var plantingEditMode by remember(bencao.id) { mutableStateOf(false) }

    LaunchedEffect(showPlantingDialog, bencao.id) {
        if (showPlantingDialog) {
            plantingDraft = bencao.plantingMethod
            plantingFetchError = null
        }
    }

    var editName by rememberSaveable(bencao.id) { mutableStateOf("") }
    var editFamily by rememberSaveable(bencao.id) { mutableStateOf("") }
    var editGenus by rememberSaveable(bencao.id) { mutableStateOf("") }
    var editSpecies by rememberSaveable(bencao.id) { mutableStateOf("") }
    var editDescription by rememberSaveable(bencao.id) { mutableStateOf("") }

    LaunchedEffect(showEditDialog, bencao.id) {
        if (showEditDialog) {
            editName = bencao.name
            editFamily = bencao.family
            editGenus = bencao.genus
            editSpecies = bencao.species
            editDescription = bencao.description
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text(text = "物种详情") },
            colors = appGreenTopAppBarColors(),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "编辑物种"
                    )
                }
            }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = Dimens.lg, vertical = Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            item {
                val layerShape = RoundedCornerShape(Dimens.radiusMd)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = layerShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.lg),
                        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                    ) {
                        Text(
                            text = "名称",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
                        ) {
                            Text(
                                text = bencao.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { showPlantingDialog = true },
                                contentPadding = PaddingValues(horizontal = Dimens.md, vertical = Dimens.sm)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Yard,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(Dimens.sm))
                                Text("栽种助手")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BencaoTaxonomyTag(label = "科", value = bencao.family)
                            BencaoTaxonomyTag(label = "属", value = bencao.genus)
                            BencaoTaxonomyTag(label = "种", value = bencao.species)
                        }
                    }
                }
            }
            item {
                val recessedShape = RoundedCornerShape(Dimens.radiusMd)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = recessedShape
                        ),
                    shape = recessedShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.lg),
                        verticalArrangement = Arrangement.spacedBy(Dimens.md)
                    ) {
                        val rarityLevel = bencao.rarity.coerceIn(1, 5)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "稀有度",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = if (i <= rarityLevel) Icons.Filled.Star else Icons.Outlined.Star,
                                        contentDescription = if (i <= rarityLevel) "实心星" else "空心星",
                                        modifier = Modifier.size(22.dp),
                                        tint = if (i <= rarityLevel) {
                                            Color(0xFFFFB300)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                        }
                                    )
                                }
                            }
                        }
                        Text(
                            text = "描述",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = bencao.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimens.xs),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BencaoDetailStatusDisk(
                                label = "有毒",
                                active = bencao.isToxic,
                                icon = Icons.Outlined.Warning
                            )
                            BencaoDetailStatusDisk(
                                label = "保护",
                                active = bencao.isProtectedSpecies,
                                icon = Icons.Outlined.Shield
                            )
                            BencaoDetailStatusDisk(
                                label = "入侵",
                                active = bencao.isInvasiveSpecies,
                                icon = Icons.Outlined.PestControl
                            )
                        }
                        if (bencao.isToxic || bencao.isProtectedSpecies || bencao.isInvasiveSpecies) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = Dimens.sm),
                                verticalArrangement = Arrangement.spacedBy(Dimens.xs)
                            ) {
                                if (bencao.isToxic) {
                                    Text(
                                        text = "已识别到有毒物种，请提醒身边人注意防范",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (bencao.isProtectedSpecies) {
                                    Text(
                                        text = "已识别到保护物种，可向有关部门报告",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (bencao.isInvasiveSpecies) {
                                    Text(
                                        text = "已识别到入侵物种，可向有关部门报告",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = Dimens.xs),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                        )
                        Text(
                            text = "创建时间",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = createdAtText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            item {
                val galleryShape = RoundedCornerShape(Dimens.radiusMd)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = galleryShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.lg),
                        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "图片（${bencao.images.size} / 10）",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Button(
                                onClick = onAppendPhotoClick,
                                enabled = appendPhotoEnabled,
                                contentPadding = PaddingValues(horizontal = Dimens.md, vertical = Dimens.sm)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(Dimens.sm))
                                Text("添加照片")
                            }
                        }
                        if (!appendPhotoEnabled) {
                            Text(
                                text = "已达 10 张上限，无法继续添加",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (bencao.images.isEmpty()) {
                            Text(
                                text = "暂无照片",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(128.dp)
                                    .padding(top = Dimens.xs),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
                            ) {
                                items(
                                    count = bencao.images.size,
                                    key = { index -> index }
                                ) { index ->
                                    val uri = bencao.images[index]
                                    Box(
                                        modifier = Modifier
                                            .size(128.dp)
                                            .clip(RoundedCornerShape(Dimens.radiusSm))
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(Dimens.radiusSm)
                                            )
                                            .clickable {
                                                onImageClick(
                                                    bencao.images,
                                                    index,
                                                    bencao.name,
                                                    bencao.createdAt.time
                                                )
                                            }
                                    ) {
                                        Image(
                                            painter = rememberBencaoGalleryThumbPainter(uri),
                                            contentDescription = "本草图片",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (bencao.images.size >= 2) {
                                            IconButton(
                                                onClick = { pendingDeleteImageIndex = index },
                                                modifier = Modifier.align(Alignment.TopEnd)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = "删除图片",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDeleteImageIndex?.let { idx ->
        AlertDialog(
            onDismissRequest = { pendingDeleteImageIndex = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这张图片吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = idx
                        pendingDeleteImageIndex = null
                        onDeletePhoto(toDelete)
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteImageIndex = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("编辑物种信息") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editFamily,
                        onValueChange = { editFamily = it },
                        label = { Text("科") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editGenus,
                        onValueChange = { editGenus = it },
                        label = { Text("属") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSpecies,
                        onValueChange = { editSpecies = it },
                        label = { Text("种") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("描述") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newModel = bencao.copy(
                            name = editName,
                            family = editFamily,
                            genus = editGenus,
                            species = editSpecies,
                            description = editDescription
                        )
                        showEditDialog = false
                        onUpdateBencao(newModel)
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showPlantingDialog) {
        Dialog(
            onDismissRequest = {
                if (!plantingFetchLoading) showPlantingDialog = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(0.88f),
                shape = RoundedCornerShape(Dimens.radiusMd),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(Dimens.lg)
                ) {
                    Text(
                        text = "栽种方式",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Dimens.md))
                    if (!plantingEditMode && plantingDraft.isNotBlank()) {
                        Markdown(
                            content = plantingDraft,
                            typography = markdownTypography(
                                h1 = MaterialTheme.typography.titleMedium,
                                h2 = MaterialTheme.typography.titleSmall,
                                h3 = MaterialTheme.typography.titleSmall,
                                h4 = MaterialTheme.typography.bodyLarge,
                                h5 = MaterialTheme.typography.bodyMedium,
                                h6 = MaterialTheme.typography.bodyMedium,
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        )
                    } else {
                        OutlinedTextField(
                            value = plantingDraft,
                            onValueChange = { plantingDraft = it },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            placeholder = { Text("在此填写或由下方按钮生成栽种说明…") },
                            minLines = 8,
                            maxLines = 18
                        )
                    }
                    if (plantingDraft.isNotBlank()) {
                        Spacer(modifier = Modifier.height(Dimens.xs))
                        TextButton(
                            onClick = { plantingEditMode = !plantingEditMode },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(if (plantingEditMode) "预览" else "编辑")
                        }
                    }
                    plantingFetchError?.let { err ->
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = Dimens.xs)
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.md))
                    if (plantingFetchLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(Dimens.sm))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                if (!plantingFetchLoading) showPlantingDialog = false
                            },
                            enabled = !plantingFetchLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("取消")
                        }
                        Button(
                            onClick = {
                                val q = bencao.displayNameForPlantingPrompt()
                                if (q.isBlank()) {
                                    Toast.makeText(context, "名称为空，无法获取栽种方式", Toast.LENGTH_SHORT).show()
                                } else {
                                    plantingFetchError = null
                                    scope.launch {
                                        plantingFetchLoading = true
                                        try {
                                            plantingDraft = DoubaoAi.fetchPlantingMethod(context, q)
                                            plantingEditMode = false
                                        } catch (t: Throwable) {
                                            val msg = t.message ?: "获取失败"
                                            plantingFetchError = msg
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        } finally {
                                            plantingFetchLoading = false
                                        }
                                    }
                                }
                            },
                            enabled = !plantingFetchLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (plantingFetchLoading) "Ai获取中…" else "Ai获取",
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                        Button(
                            onClick = {
                                if (!plantingFetchLoading) {
                                    onUpdateBencao(bencao.copy(plantingMethod = plantingDraft.trim()))
                                    showPlantingDialog = false
                                }
                            },
                            enabled = !plantingFetchLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("保存")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PlantingMethodBencaoListCard(
    bencao: Bencao,
    onClick: () -> Unit,
    onImageClick: (List<String>, Int, String, Long) -> Unit,
    onRequestMarkPlantingSuccess: () -> Unit
) {
    val consumeCheckClicksInteraction = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(Dimens.radiusMd),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.lg)
                    .clickable(onClick = onClick)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = bencao.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = Dimens.sm)
                    )
                    if (bencao.images.isNotEmpty()) {
                        Image(
                            painter = rememberBencaoListThumbnailPainter(bencao.images[0]),
                            contentDescription = "本草图片",
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(Dimens.radiusSm))
                                .clickable {
                                    onImageClick(
                                        bencao.images,
                                        0,
                                        bencao.name,
                                        bencao.createdAt.time
                                    )
                                },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(Dimens.radiusSm))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "无图片",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(Dimens.lg))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "栽种方式",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = Dimens.sm)
                    )
                    Markdown(
                        content = bencao.plantingMethod,
                        typography = markdownTypography(
                            h1 = MaterialTheme.typography.titleMedium,
                            h2 = MaterialTheme.typography.titleSmall,
                            h3 = MaterialTheme.typography.titleSmall,
                            h4 = MaterialTheme.typography.bodyLarge,
                            h5 = MaterialTheme.typography.bodyMedium,
                            h6 = MaterialTheme.typography.bodyMedium,
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            val checkVector = if (bencao.isSuccess) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline
            val checkTint = if (bencao.isSuccess) {
                BencaoGreen
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            val checkAreaModifier = if (bencao.isSuccess) {
                Modifier.clickable(
                    interactionSource = consumeCheckClicksInteraction,
                    indication = null,
                    onClick = {}
                )
            } else {
                Modifier.clickable(onClick = onRequestMarkPlantingSuccess)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .then(checkAreaModifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = checkVector,
                    contentDescription = if (bencao.isSuccess) "已标记栽种成功" else "确认栽种成功",
                    tint = checkTint,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PlantingMethodDetailScreen(
    bencao: Bencao,
    onBack: () -> Unit,
    onGoToSpeciesDetail: () -> Unit,
    onRequestMarkPlantingSuccess: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text(text = "栽种方式") },
            colors = appGreenTopAppBarColors(),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                val checkVector = if (bencao.isSuccess) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline
                val checkTint = if (bencao.isSuccess) {
                    BencaoGreenOnContainer
                } else {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                }
                if (bencao.isSuccess) {
                    Icon(
                        imageVector = checkVector,
                        contentDescription = "已标记栽种成功",
                        tint = checkTint,
                        modifier = Modifier
                            .padding(end = Dimens.md)
                            .size(26.dp)
                    )
                } else {
                    IconButton(onClick = onRequestMarkPlantingSuccess) {
                        Icon(
                            imageVector = checkVector,
                            contentDescription = "确认栽种成功",
                            tint = checkTint,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = Dimens.lg, vertical = Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            item {
                val layerShape = RoundedCornerShape(Dimens.radiusMd)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = layerShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.lg),
                        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                    ) {
                        Text(
                            text = "名称",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
                        ) {
                            Text(
                                text = bencao.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            OutlinedButton(
                                onClick = onGoToSpeciesDetail,
                                contentPadding = PaddingValues(horizontal = Dimens.sm, vertical = Dimens.sm)
                            ) {
                                Text("转到详情")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BencaoTaxonomyTag(label = "科", value = bencao.family)
                            BencaoTaxonomyTag(label = "属", value = bencao.genus)
                            BencaoTaxonomyTag(label = "种", value = bencao.species)
                        }
                    }
                }
            }
            item {
                val bottomShape = RoundedCornerShape(Dimens.radiusMd)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = bottomShape
                        ),
                    shape = bottomShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.lg)
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                    ) {
                        Text(
                            text = "栽种方式",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Markdown(
                            content = bencao.plantingMethod,
                            typography = markdownTypography(
                                h1 = MaterialTheme.typography.titleMedium,
                                h2 = MaterialTheme.typography.titleSmall,
                                h3 = MaterialTheme.typography.titleSmall,
                                h4 = MaterialTheme.typography.bodyLarge,
                                h5 = MaterialTheme.typography.bodyMedium,
                                h6 = MaterialTheme.typography.bodyMedium,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PlantingMethodsLibraryScreen(
    onBack: () -> Unit,
    onNavigateToSpeciesDetail: (Long) -> Unit,
    onSharePosterSuccess: (() -> Unit)? = null,
) {
    var currentPage by remember { mutableIntStateOf(0) }
    var sortMode by remember { mutableStateOf(LibrarySortMode.CreatedAtDesc) }
    var plantingDetailId by remember { mutableStateOf<Long?>(null) }
    var fullscreenImages by remember { mutableStateOf<BencaoFullscreenSession?>(null) }
    var successDialogBencao by remember { mutableStateOf<Bencao?>(null) }
    val scope = rememberCoroutineScope()
    val pageSize = 10

    val listFlow = remember(sortMode, currentPage, pageSize) {
        BencaoRepository.observePlantingMethodLibraryPage(sortMode, currentPage, pageSize)
    }
    val bencaos by listFlow.collectAsState(initial = emptyList())

    val totalPages by BencaoRepository.observePlantingMethodTotalPages(pageSize)
        .collectAsState(initial = 1)

    LaunchedEffect(totalPages, currentPage) {
        if (currentPage >= totalPages) {
            currentPage = maxOf(0, totalPages - 1)
        }
    }

    val detailId = plantingDetailId
    if (detailId != null) {
        val detailFlow = remember(detailId) { BencaoRepository.observeBencaoById(detailId) }
        val detailBencao by detailFlow.collectAsState(initial = null)
        val model = detailBencao
        when {
            model == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            model.plantingMethod.isBlank() -> {
                LaunchedEffect(model.id) {
                    plantingDetailId = null
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                PlantingMethodDetailScreen(
                    bencao = model,
                    onBack = { plantingDetailId = null },
                    onGoToSpeciesDetail = { onNavigateToSpeciesDetail(model.id) },
                    onRequestMarkPlantingSuccess = { successDialogBencao = model }
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .heightIn(min = 56.dp)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "返回",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Text(
                        text = "已知栽种方式",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sortMode.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
                        )
                        Spacer(modifier = Modifier.width(Dimens.sm))
                        IconButton(
                            onClick = {
                                sortMode = sortMode.next()
                                currentPage = 0
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SwapVert,
                                contentDescription = "切换排序",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            if (bencaos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "暂无已填写的栽种方式")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(Dimens.lg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.lg)
                ) {
                    items(bencaos, key = { it.id }) { bencao ->
                        PlantingMethodBencaoListCard(
                            bencao = bencao,
                            onClick = { plantingDetailId = bencao.id },
                            onImageClick = { uris, index, name, createdAt ->
                                fullscreenImages = BencaoFullscreenSession(uris, index, name, createdAt)
                            },
                            onRequestMarkPlantingSuccess = { successDialogBencao = bencao }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.lg),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentPage > 0) currentPage-- },
                        enabled = currentPage > 0
                    ) {
                        Text("上一页")
                    }
                    Text(
                        text = "${currentPage + 1} / $totalPages",
                        modifier = Modifier.padding(horizontal = Dimens.lg)
                    )
                    OutlinedButton(
                        onClick = { if (currentPage < totalPages - 1) currentPage++ },
                        enabled = currentPage < totalPages - 1
                    ) {
                        Text("下一页")
                    }
                }
            }
        }
    }

    successDialogBencao?.let { b ->
        Dialog(onDismissRequest = { successDialogBencao = null }) {
            Card(
                shape = RoundedCornerShape(Dimens.radiusMd),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.lg, vertical = Dimens.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val questionStyle = MaterialTheme.typography.bodyLarge
                    Text(
                        text = "已确定栽种成功了吗？",
                        style = questionStyle.copy(
                            fontSize = questionStyle.fontSize * 1.5f
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Dimens.lg))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = { successDialogBencao = null }) {
                            Text("否")
                        }
                        Spacer(modifier = Modifier.width(Dimens.md))
                        Button(
                            onClick = {
                                successDialogBencao = null
                                scope.launch {
                                    BencaoRepository.updateBencao(b.copy(isSuccess = true))
                                    plantingDetailId = null
                                }
                            }
                        ) {
                            Text("是！")
                        }
                    }
                }
            }
        }
    }

    fullscreenImages?.let { session ->
        BencaoImageFullscreenOverlay(
            imageUris = session.imageUris,
            initialPage = session.initialPage,
            bencaoName = session.bencaoName,
            createdAtMillis = session.createdAtMillis,
            onDismiss = { fullscreenImages = null },
            onSharePosterSuccess = onSharePosterSuccess,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddBencaoScreen(
    imageUri: Uri,
    onSave: (Bencao) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var family by remember { mutableStateOf("") }
    var genus by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rarity by remember { mutableStateOf(1) }
    var isToxic by remember { mutableStateOf(false) }
    var isProtectedSpecies by remember { mutableStateOf(false) }
    var isInvasiveSpecies by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var aiLoading by remember { mutableStateOf(false) }
    var aiError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "新增本草") },
                colors = appGreenTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        // 避免 Scaffold 默认把 status bar inset 再加到内容区（TopAppBar 已处理）
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = Dimens.lg, vertical = Dimens.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.lg)
                ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !saving && !aiLoading
                ) {
                    Text("取消")
                }

                OutlinedButton(
                    onClick = {
                        if (aiLoading) return@OutlinedButton
                        aiError = null
                        scope.launch {
                            aiLoading = true
                            try {
                                val suggestion = DoubaoAi.analyzeBencaoImage(
                                    context = context,
                                    imageUri = imageUri,
                                    maxDescriptionCodePoints = 128
                                )
                                name = suggestion.name
                                family = suggestion.family
                                genus = suggestion.genus
                                species = suggestion.species
                                description = suggestion.description
                                rarity = suggestion.rarity
                                isToxic = suggestion.isToxic
                                isProtectedSpecies = suggestion.isProtectedSpecies
                                isInvasiveSpecies = suggestion.isInvasiveSpecies
                            } catch (t: Throwable) {
                                aiError = t.message ?: "Ai识图失败"
                            } finally {
                                aiLoading = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !aiLoading && !saving
                ) {
                    Text(if (aiLoading) "识别中…" else "Ai识图")
                }

                Button(
                    onClick = {
                        scope.launch {
                            saving = true
                            try {
                                val bencao = Bencao(
                                    name = name.trim(),
                                    family = family.trim(),
                                    genus = genus.trim(),
                                    species = species.trim(),
                                    description = description,
                                    images = listOf(imageUri.toString()),
                                    rarity = rarity,
                                    isToxic = isToxic,
                                    isProtectedSpecies = isProtectedSpecies,
                                    isInvasiveSpecies = isInvasiveSpecies,
                                    plantingMethod = "",
                                    isSuccess = false
                                )
                                onSave(bencao)
                            } finally {
                                saving = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !saving && !aiLoading && name.isNotBlank()
                ) {
                    Text(if (saving) "保存中…" else "保存")
                }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(visible = aiLoading || saving) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.lg)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.lg)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "拍摄的照片",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(Dimens.radiusMd))
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    OutlinedTextField(
                        value = family,
                        onValueChange = { family = it },
                        label = { Text("科") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = genus,
                        onValueChange = { genus = it },
                        label = { Text("属") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = species,
                        onValueChange = { species = it },
                        label = { Text("种") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("稀有度", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (i in 1..5) {
                                val filled = i <= rarity
                                IconButton(
                                    onClick = { rarity = i },
                                    enabled = !saving && !aiLoading
                                ) {
                                    Icon(
                                        imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
                                        contentDescription = "稀有度 $i",
                                        tint = if (filled) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("是否有毒物种", style = MaterialTheme.typography.titleSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isToxic) "是" else "否", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(Dimens.sm))
                            Switch(
                                checked = isToxic,
                                onCheckedChange = { isToxic = it },
                                enabled = !saving && !aiLoading
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("是否保护物种", style = MaterialTheme.typography.titleSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isProtectedSpecies) "是" else "否", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(Dimens.sm))
                            Switch(
                                checked = isProtectedSpecies,
                                onCheckedChange = { isProtectedSpecies = it },
                                enabled = !saving && !aiLoading
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("是否入侵物种", style = MaterialTheme.typography.titleSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isInvasiveSpecies) "是" else "否", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(Dimens.sm))
                            Switch(
                                checked = isInvasiveSpecies,
                                onCheckedChange = { isInvasiveSpecies = it },
                                enabled = !saving && !aiLoading
                            )
                        }
                    }
                }

                aiError?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}



@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun BencaoDetailByIdScreen(
    bencaoId: Long,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var fullscreenImages by remember { mutableStateOf<BencaoFullscreenSession?>(null) }
    var appendPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var appendPhotoBencaoId by remember { mutableStateOf<Long?>(null) }

    val appendCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = appendPhotoUri
        val id = appendPhotoBencaoId
        appendPhotoUri = null
        appendPhotoBencaoId = null
        if (success && uri != null && id != null) {
            scope.launch {
                val current = BencaoRepository.getBencaoById(id)
                if (current != null && current.images.size < 10) {
                    BencaoRepository.updateBencao(
                        current.copy(images = current.images + uri.toString())
                    )
                }
            }
        }
    }

    val appendCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val id = appendPhotoBencaoId
            if (id != null) {
                val uri = createImageUri(ctx)
                appendPhotoUri = uri
                appendCameraLauncher.launch(uri)
            }
        } else {
            appendPhotoBencaoId = null
        }
    }

    fun startAppendPhoto(id: Long) {
        appendPhotoBencaoId = id
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) -> {
                val uri = createImageUri(ctx)
                appendPhotoUri = uri
                appendCameraLauncher.launch(uri)
            }
            else -> {
                appendCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    val detailFlow = remember(bencaoId) { BencaoRepository.observeBencaoById(bencaoId) }
    val bencao by detailFlow.collectAsState(initial = null)

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            bencao == null -> {
                Scaffold(
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(text = "物种详情") },
                            colors = appGreenTopAppBarColors(),
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                        contentDescription = "返回"
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            else -> {
                val model = bencao!!
                BencaoDetailScreen(
                    bencao = model,
                    onBack = onBack,
                    onAppendPhotoClick = { startAppendPhoto(model.id) },
                    appendPhotoEnabled = model.images.size < 10,
                    onUpdateBencao = { updated ->
                        scope.launch { BencaoRepository.updateBencao(updated) }
                    },
                    onImageClick = { uris, index, name, createdAt ->
                        fullscreenImages = BencaoFullscreenSession(uris, index, name, createdAt)
                    },
                    onDeletePhoto = { index ->
                        scope.launch {
                            val current = BencaoRepository.getBencaoById(model.id) ?: return@launch
                            if (current.images.size < 2) return@launch
                            if (index !in current.images.indices) return@launch
                            val newImages = current.images.toMutableList().apply { removeAt(index) }
                            if (newImages.isEmpty()) return@launch
                            BencaoRepository.updateBencao(current.copy(images = newImages))
                        }
                    },
                )
            }
        }

        fullscreenImages?.let { session ->
            BencaoImageFullscreenOverlay(
                imageUris = session.imageUris,
                initialPage = session.initialPage,
                bencaoName = session.bencaoName,
                createdAtMillis = session.createdAtMillis,
                onDismiss = { fullscreenImages = null }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeScreen(
    backupBlocking: Boolean,
    onShootIdentify: () -> Unit,
    onOpenSpeciesLibrary: () -> Unit,
    onOpenPlantingMethods: () -> Unit,
    onOpenAiSettings: () -> Unit,
    onOpenBencaoDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val allBencaos by BencaoRepository.observeAllBencaos().collectAsState(initial = emptyList())
    val latestTwo = remember(allBencaos) { allBencaos.take(2) }
    val today = remember { LocalDate.now() }
    val activityPosterUris = remember(today) {
        ActivityPosterResolver.resolvePosterAssetUris(context, today)
    }
    val tileGreen = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((200f * 2f / 3f).dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = Dimens.lg, vertical = Dimens.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.5.dp, BencaoBrandAccent, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "F",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BencaoBrandAccent
                        )
                    }
                    Text(
                        text = "繁草APP",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BencaoBrandAccent
                    )
                }
                Text(
                    text = "热爱生活的开始",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.95f),
                    letterSpacing = (2f / 3f).em
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.md))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.lg),
            horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
        ) {
            HomePrimaryTile(
                modifier = Modifier.weight(1f),
                enabled = !backupBlocking,
                tileGreen = tileGreen,
                icon = Icons.Outlined.PhotoCamera,
                label = "拍摄识图",
                onClick = onShootIdentify
            )
            HomePrimaryTile(
                modifier = Modifier.weight(1f),
                enabled = !backupBlocking,
                tileGreen = tileGreen,
                icon = Icons.Outlined.FolderOpen,
                label = "收藏浏览",
                onClick = onOpenSpeciesLibrary
            )
        }

        Spacer(modifier = Modifier.height(Dimens.sm))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.lg),
            horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
        ) {
            HomePrimaryTile(
                modifier = Modifier.weight(1f),
                enabled = !backupBlocking,
                tileGreen = tileGreen,
                icon = Icons.Outlined.Yard,
                label = "栽种",
                onClick = onOpenPlantingMethods
            )
            HomePrimaryTile(
                modifier = Modifier.weight(1f),
                enabled = !backupBlocking,
                tileGreen = tileGreen,
                icon = Icons.Outlined.FlashOn,
                label = "AI",
                onClick = onOpenAiSettings
            )
        }

        Spacer(modifier = Modifier.height(Dimens.lg))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm)
        ) {
            if (activityPosterUris.isNotEmpty()) {
                HomeActivityPosterCarousel(posterUris = activityPosterUris)
            }
            for (b in latestTwo) {
                HomeDiscoveryCard(
                    bencao = b,
                    onClick = {
                        if (!backupBlocking) onOpenBencaoDetail(b.id)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.xl))
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MineScreen(
    onBackupOverlayChange: (SpeciesBackupOverlayUi?) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 旧的"模拟登录"开关已废弃：现在统一走后端 token 登录态
    var importCandidates by remember { mutableStateOf<List<Bencao>?>(null) }
    var ioBusy by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { onBackupOverlayChange(null) }
    }

    val stamp = remember {
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            ioBusy = true
            onBackupOverlayChange(SpeciesBackupOverlayUi(title = "正在准备导出…", stepDone = 0, stepTotal = 0))
            try {
                val snapshot = withContext(Dispatchers.IO) {
                    BencaoRepository.getAllBencaosSnapshot()
                }
                val totalSteps = snapshot.sumOf { it.images.size } + 1
                onBackupOverlayChange(
                    SpeciesBackupOverlayUi(title = "正在导出 ZIP", stepDone = 0, stepTotal = totalSteps)
                )
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
                        BencaoSpeciesBackupZip.exportZip(context, snapshot, out) { done, total ->
                            scope.launch(Dispatchers.Main.immediate) {
                                onBackupOverlayChange(
                                    SpeciesBackupOverlayUi(title = "正在导出 ZIP", stepDone = done, stepTotal = total)
                                )
                            }
                        }
                    } ?: error("无法写入所选位置")
                }
                Toast.makeText(
                    context,
                    "已导出 ZIP（${snapshot.size} 条物种，含图片）",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "导出失败：${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                ioBusy = false
                onBackupOverlayChange(null)
            }
        }
    }

    val importPickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            ioBusy = true
            onBackupOverlayChange(SpeciesBackupOverlayUi(title = "正在解压备份…", stepDone = 0, stepTotal = 0))
            try {
                val parsed = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        BencaoSpeciesBackupZip.importZip(context, input) { done, total ->
                            scope.launch(Dispatchers.Main.immediate) {
                                val overlay = if (total == 0) {
                                    SpeciesBackupOverlayUi(title = "正在解压备份…", stepDone = 0, stepTotal = 0)
                                } else {
                                    SpeciesBackupOverlayUi(title = "正在导入数据", stepDone = done, stepTotal = total)
                                }
                                onBackupOverlayChange(overlay)
                            }
                        }.getOrElse { err -> throw err }
                    } ?: error("无法读取文件")
                }
                importCandidates = parsed
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "读取失败：${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                ioBusy = false
                onBackupOverlayChange(null)
            }
        }
    }

    importCandidates?.let { list ->
        AlertDialog(
            onDismissRequest = { if (!ioBusy) importCandidates = null },
            title = { Text(text = "导入物种") },
            text = {
                Text(
                    text = "读取到 ${list.size} 条记录。\n\n" +
                        "合并导入：保留本地已有数据并追加。\n" +
                        "替换导入：先清空本地资料库再写入备份（谨慎）。",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Column {
                    TextButton(
                        enabled = !ioBusy,
                        onClick = {
                            val batch = list
                            importCandidates = null
                            scope.launch {
                                ioBusy = true
                                onBackupOverlayChange(
                                    SpeciesBackupOverlayUi(title = "正在写入数据库…", stepDone = 0, stepTotal = 0)
                                )
                                try {
                                    BencaoRepository.importBencaosMerge(batch)
                                    Toast.makeText(
                                        context,
                                        "已合并导入 ${batch.size} 条",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "导入失败：${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    ioBusy = false
                                    onBackupOverlayChange(null)
                                }
                            }
                        }
                    ) { Text(text = "合并导入") }
                    TextButton(
                        enabled = !ioBusy,
                        onClick = {
                            val batch = list
                            importCandidates = null
                            scope.launch {
                                ioBusy = true
                                onBackupOverlayChange(
                                    SpeciesBackupOverlayUi(title = "正在写入数据库…", stepDone = 0, stepTotal = 0)
                                )
                                try {
                                    BencaoRepository.importBencaosReplaceAll(batch)
                                    Toast.makeText(
                                        context,
                                        "已替换为 ${batch.size} 条",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "导入失败：${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    ioBusy = false
                                    onBackupOverlayChange(null)
                                }
                            }
                        }
                    ) { Text(text = "替换导入") }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !ioBusy,
                    onClick = { importCandidates = null }
                ) { Text(text = "取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "我的") },
                colors = appGreenTopAppBarColors()
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = Dimens.lg, vertical = Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                    MineSectionTitle(text = "资料备份")
                    MineSurfaceGroup {
                        MineLeadingIconRow(
                            icon = Icons.Outlined.UploadFile,
                            headline = "本地备份（ZIP）",
                            supporting = "单一文件导出物种字段与图片，便于换机或其它客户端导入。"
                        )
                        Text(
                            text = "schema ${BencaoSpeciesBackupZip.SCHEMA_ID}，formatVersion ${BencaoSpeciesBackupZip.FORMAT_VERSION}；可选字段或缺失图片时尽力导出/导入，对应项留空。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                val fileName =
                                    "bencaoclient_species_${stamp.format(LocalDateTime.now())}.zip"
                                exportLauncher.launch(fileName)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !ioBusy
                        ) { Text(text = "导出全部") }
                        OutlinedButton(
                            onClick = {
                                importPickLauncher.launch(
                                    arrayOf(
                                        "application/zip",
                                        "application/x-zip-compressed",
                                        "*/*"
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !ioBusy
                        ) { Text(text = "导入备份") }
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(top = Dimens.sm),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "版本 v${BuildConfig.VERSION_NAME}-${BuildConfig.BUILD_TAG}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.md, bottom = Dimens.lg),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    BencaoclientTheme {
        MainScreen()
    }
}
