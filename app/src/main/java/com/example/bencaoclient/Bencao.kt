package com.example.bencaoclient

import java.util.Date

data class Bencao(
    /** Room 主键；新建时为 0，插入后由数据库分配 */
    val id: Long = 0L,
    val name: String, // 短字符
    /** 科 */
    val family: String = "",
    /** 属 */
    val genus: String = "",
    /** 种 */
    val species: String = "",
    val description: String, // 长文本
    val images: List<String>, // 最多10张图片
    /** 稀有度：1-5 */
    val rarity: Int = 1,
    /** 是否有毒 */
    val isToxic: Boolean = false,
    /** 是否保护物种 */
    val isProtectedSpecies: Boolean = false,
    /** 是否入侵物种 */
    val isInvasiveSpecies: Boolean = false,
    /** 栽种方式说明（可由栽种助手生成后保存） */
    val plantingMethod: String = "",
    /** 栽种是否成功等标记，默认否 */
    val isSuccess: Boolean = false,
    val createdAt: Date = Date() // 创建时间
) {
    init {
        require(images.size <= 10) { "Images list cannot exceed 10 items" }
        require(rarity in 1..5) { "rarity must be in 1..5" }
    }
}

/**
 * 仅用于「获取栽种方式」一次请求中的物种名：若 [name] 以「疑似」开头则临时去掉该前缀，便于模型按真实类群回答。
 * **不修改**本条记录已保存的 [name]；识图结果仍可长期保留「疑似xxx」。
 */
fun Bencao.displayNameForPlantingPrompt(): String {
    val t = name.trim()
    if (!t.startsWith("疑似")) return t
    val stripped = t.removePrefix("疑似").trim()
    return stripped.ifBlank { t }
}
