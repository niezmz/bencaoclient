package com.example.bencaoclient.util

import com.example.bencaoclient.Bencao

const val LibrarySearchMaxNameQueryCodePoints = 6

fun trimToMaxCodePoints(input: String, maxCodePoints: Int): String {
    if (input.isEmpty()) return input
    val count = input.codePointCount(0, input.length)
    if (count <= maxCodePoints) return input
    val endIndex = input.offsetByCodePoints(0, maxCodePoints)
    return input.substring(0, endIndex)
}

fun rankBencaosByNameRelevance(all: List<Bencao>, query: String): List<Bencao> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    return all
        .map { b -> b to bencaoNameRelevanceScore(b.name, q) }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }
        .map { it.first }
}

fun bencaoNameRelevanceScore(name: String, query: String): Int {
    val n = name.trim()
    val q = query.trim()
    if (q.isEmpty()) return 0
    when {
        n == q -> return 1_000_000
        n.startsWith(q) -> return 800_000
        n.contains(q) -> return 600_000 - n.indexOf(q).coerceAtMost(99_000)
    }
    var searchStart = 0
    var matched = 0
    var firstPos = -1
    var lastPos = -1
    var i = 0
    while (i < q.length) {
        val cp = q.codePointAt(i)
        val chStr = Character.toString(cp)
        val found = n.indexOf(chStr, searchStart)
        if (found < 0) break
        if (firstPos < 0) firstPos = found
        lastPos = found
        matched++
        searchStart = found + chStr.length
        i += Character.charCount(cp)
    }
    if (matched == 0) return 0
    val span = if (lastPos >= firstPos) lastPos - firstPos else 0
    return matched * 10_000 - span
}
