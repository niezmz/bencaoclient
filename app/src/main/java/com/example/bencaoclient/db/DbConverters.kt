package com.example.bencaoclient.db

import androidx.room.TypeConverter
import org.json.JSONArray

object DbConverters {
    @TypeConverter
    @JvmStatic
    fun imagesToJson(images: List<String>): String {
        val array = JSONArray()
        images.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    @JvmStatic
    fun jsonToImages(json: String): List<String> {
        return runCatching {
            val array = JSONArray(json)
            List(array.length()) { idx -> array.optString(idx) }
        }.getOrDefault(emptyList())
    }
}

