package com.example.bencaoclient.activity

import android.content.Context
import java.time.LocalDate

/**
 * 按客户端本地日期从 [ASSET_DIR] 解析活动海报资源。
 * 命名：`MM-H-Z.ext`（如 `05-0-1.png`），MM 为月份，H 为 0（1–15 日）或 1（16 日及以后），Z 为同组序号。
 */
object ActivityPosterResolver {
    private const val ASSET_DIR = "activity/posters"
    private const val ASSET_URI_PREFIX = "file:///android_asset/$ASSET_DIR/"

    fun resolvePosterAssetUris(
        context: Context,
        date: LocalDate = LocalDate.now(),
    ): List<String> {
        val month = "%02d".format(date.monthValue)
        val half = if (date.dayOfMonth <= 15) "0" else "1"
        val prefix = "$month-$half-"
        val names = context.assets.list(ASSET_DIR)?.orEmpty() ?: return emptyList()
        return names
            .filter { name ->
                !name.startsWith('.') && name.startsWith(prefix) && isImageFile(name)
            }
            .sortedBy { sequenceIndex(it) }
            .map { "$ASSET_URI_PREFIX$it" }
    }

    private fun isImageFile(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".png") ||
            lower.endsWith(".jpg") ||
            lower.endsWith(".jpeg") ||
            lower.endsWith(".webp")
    }

    /** 从 `MM-H-Z` 文件名解析 Z，无法解析时排到末尾。 */
    private fun sequenceIndex(fileName: String): Int {
        val base = fileName.substringBeforeLast('.')
        return base.substringAfterLast('-').toIntOrNull() ?: Int.MAX_VALUE
    }
}
