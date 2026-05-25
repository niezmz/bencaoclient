package com.example.bencaoclient.model

/** 资料导出/导入遮罩状态：[stepTotal]==0 时为不定进度（如解压）；否则显示 [stepDone]/[stepTotal]。 */
data class SpeciesBackupOverlayUi(
    val title: String,
    val stepDone: Int = 0,
    val stepTotal: Int = 0
)

/** 全屏看图入参：同一物种下多张图可滑动，分享海报使用 [bencaoName]。 */
data class BencaoFullscreenSession(
    val imageUris: List<String>,
    val initialPage: Int,
    val bencaoName: String,
    /** [Bencao.createdAt]，用于海报底部时间与右侧金色日期点缀 */
    val createdAtMillis: Long = 0L
)

data class NavigationItem(val name: String, val id: Int)
