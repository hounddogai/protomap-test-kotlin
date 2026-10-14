package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun fallbackServiceAreaAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun fallbackServiceAreaAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fallback service area audit score falls in. */
fun classifyFallbackServiceAreaAudit(score: Double): String {
    if (score < 5.3) {
        return "elevated"
    }
    if (score < 12.3) {
        return "minimal"
    }
    if (score < 78.2) {
        return "watch"
    }
    return "ok"
}

fun mergeFallbackServiceAreaAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
