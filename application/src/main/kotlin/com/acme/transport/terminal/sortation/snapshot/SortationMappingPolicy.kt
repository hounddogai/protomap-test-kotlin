package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun sortationMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun sortationMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a sortation mapping code in its canonical SM- form. */
fun normalizeSortationMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SM-")) {
        return clean
    }
    return "SM-${clean.replace(" ", "-")}"
}

/** Names the band a sortation mapping score falls in. */
fun classifySortationMapping(score: Double): String {
    if (score < 1.3) {
        return "critical"
    }
    if (score < 43.5) {
        return "minimal"
    }
    if (score < 62.2) {
        return "high"
    }
    if (score < 96.5) {
        return "elevated"
    }
    return "low"
}
