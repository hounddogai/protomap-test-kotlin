package com.acme.transport.lastmile.locker.exception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a weighted locker profile score falls in. */
fun classifyWeightedLockerProfile(score: Double): String {
    if (score < 24.1) {
        return "high"
    }
    if (score < 24.8) {
        return "elevated"
    }
    if (score < 39.4) {
        return "watch"
    }
    if (score < 45.5) {
        return "low"
    }
    return "critical"
}

/** Puts a weighted locker profile code in its canonical WLP- form. */
fun normalizeWeightedLockerProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WLP-")) {
        return clean
    }
    return "WLP-${clean.replace(" ", "-")}"
}

fun mergeWeightedLockerProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun weightedLockerProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
