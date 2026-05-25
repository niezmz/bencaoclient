package com.example.bencaoclient

import android.content.Context
import android.content.SharedPreferences

object ApiKeyStore {
    private const val PREFS_NAME = "ai_api_keys_prefs"
    private const val KEY_KEYS_JSON = "saved_keys_json"
    private const val KEY_ACTIVE = "active_api_key_value"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadKeys(context: Context): List<String> {
        val json = prefs(context).getString(KEY_KEYS_JSON, null) ?: return emptyList()
        return try {
            val arr = org.json.JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                arr.optString(i, null)?.takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addKey(context: Context, newKey: String) {
        val current = loadKeys(context).toMutableList()
        if (current.any { it == newKey }) return
        current.add(newKey)
        prefs(context).edit().putString(KEY_KEYS_JSON, org.json.JSONArray(current).toString()).apply()
    }

    fun deleteKey(context: Context, index: Int) {
        val current = loadKeys(context).toMutableList()
        if (index !in current.indices) return
        val removed = current.removeAt(index)
        val editor = prefs(context).edit().putString(KEY_KEYS_JSON, org.json.JSONArray(current).toString())
        if (getActiveKey(context) == removed) editor.remove(KEY_ACTIVE)
        editor.apply()
    }

    fun getActiveKey(context: Context): String? =
        prefs(context).getString(KEY_ACTIVE, null)?.takeIf { it.isNotBlank() }

    fun setActiveKey(context: Context, key: String) {
        prefs(context).edit().putString(KEY_ACTIVE, key).apply()
    }
}
