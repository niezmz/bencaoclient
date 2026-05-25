package com.example.bencaoclient.ai

import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * 兼容多种 HTTP JSON 形态：字段在根上、在 `data`/`result` 里、或内外拆分。
 * 豆包/FastAPI 常见：`{ "name":"...", "data": { "rarity": 3 } }`（name 与扩展字段分层）。
 */
internal object BackendAiSuggestionJson {

    fun parse(responseText: String, maxDescriptionCodePoints: Int): AiBencaoSuggestion {
        val root = JSONObject(responseText.trim().removePrefix("\uFEFF"))
        return parseRoot(root, maxDescriptionCodePoints)
    }

    /** 已解析出的 JSON 对象（例如 DeepSeek content 内层 JSON）同样走合并查找。 */
    fun parseRoot(root: JSONObject, maxDescriptionCodePoints: Int): AiBencaoSuggestion {
        val candidates = collectCandidateObjects(root)
        val primary = candidates.firstOrNull { it.optString("name").trim().isNotBlank() } ?: root

        val name = primary.optString("name").trim()
        var desc = primary.optString("description").trim()
        val family = primary.optString("family").trim()
        val genus = primary.optString("genus").trim()
        val species = primary.optString("species").trim()

        val rarity = candidates.pickIntInRange(
            range = 1..5,
            default = 1,
            "rarity", "Rarity", "稀有度", "稀有度等级"
        )
        val isToxic = candidates.pickBool(
            default = false,
            "isToxic", "is_toxic", "toxic", "有毒", "是否有毒"
        )
        val isProtectedSpecies = candidates.pickBool(
            default = false,
            "isProtectedSpecies", "is_protected_species", "protected", "保护物种", "是否保护物种"
        )
        val isInvasiveSpecies = candidates.pickBool(
            default = false,
            "isInvasiveSpecies", "is_invasive_species", "invasive", "入侵物种", "是否入侵物种"
        )

        if (name.isBlank()) error("AI JSON 缺少 name：${root.toString()}")
        if (desc.isBlank()) desc = "暂无描述"

        val descTrimmed = trimToMaxCodePoints(desc, maxDescriptionCodePoints)
        return AiBencaoSuggestion(
            name = name,
            description = descTrimmed,
            family = family,
            genus = genus,
            species = species,
            rarity = rarity,
            isToxic = isToxic,
            isProtectedSpecies = isProtectedSpecies,
            isInvasiveSpecies = isInvasiveSpecies
        )
    }

    private fun collectCandidateObjects(root: JSONObject): List<JSONObject> {
        val out = ArrayList<JSONObject>()
        fun add(o: JSONObject?) {
            if (o == null) return
            if (out.none { it === o }) out.add(o)
        }
        // 嵌套优先：扩展字段常在 data 里，而 name 可能在根上
        add(root.optJSONObject("data"))
        add(root.optJSONObject("result"))
        add(root.optJSONObject("payload"))
        add(root.optJSONObject("data")?.optJSONObject("suggestion"))
        add(root.optJSONObject("result")?.optJSONObject("suggestion"))
        add(root.optJSONObject("data")?.optJSONObject("result"))
        add(root.optJSONObject("flags"))
        add(root.optJSONObject("properties"))
        add(root.optJSONObject("meta"))
        add(root)
        return out
    }

    /** 大模型/中间层可能用字符串数字、或 snake_case 键名；[JSONObject.optInt] 对 String 常失败。 */
    private fun JSONObject.optIntFlexibleOrNull(vararg keys: String): Int? {
        for (key in keys) {
            if (!has(key)) continue
            val v = opt(key)
            if (v === null || v === JSONObject.NULL) continue
            val parsed = when (v) {
                is Number -> v.toInt()
                is String -> v.trim().toDoubleOrNull()?.roundToInt() ?: v.trim().toIntOrNull()
                else -> null
            }
            if (parsed != null) return parsed
        }
        return null
    }

    /** [JSONObject.optBoolean] 对 JSON 数字 0/1、字符串 "true"/"是" 等常不符合预期。 */
    private fun JSONObject.optBooleanFlexibleOrNull(vararg keys: String): Boolean? {
        for (key in keys) {
            if (!has(key)) continue
            val v = opt(key)
            if (v === null || v === JSONObject.NULL) continue
            when (v) {
                is Boolean -> return v
                is Number -> return v.toInt() != 0
                is String -> {
                    val s = v.trim().lowercase()
                    when {
                        s in setOf("true", "1", "yes", "y", "是") -> return true
                        s in setOf("false", "0", "no", "n", "否", "") -> return false
                    }
                }
            }
        }
        return null
    }

    private fun List<JSONObject>.pickIntInRange(range: IntRange, default: Int, vararg keys: String): Int {
        for (o in this) {
            val v = o.optIntFlexibleOrNull(*keys) ?: continue
            return v.coerceIn(range.first, range.last)
        }
        return default
    }

    private fun List<JSONObject>.pickBool(default: Boolean, vararg keys: String): Boolean {
        for (o in this) {
            val v = o.optBooleanFlexibleOrNull(*keys) ?: continue
            return v
        }
        return default
    }

    private fun trimToMaxCodePoints(input: String, maxCodePoints: Int): String {
        if (input.isEmpty()) return input
        val count = input.codePointCount(0, input.length)
        if (count <= maxCodePoints) return input
        val endIndex = input.offsetByCodePoints(0, maxCodePoints)
        return input.substring(0, endIndex)
    }
}
