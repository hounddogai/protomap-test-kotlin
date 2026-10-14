package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a fallback ev charge entry score falls in. */
fun classifyFallbackEvChargeEntry(score: Double): String {
    if (score < 25.3) {
        return "low"
    }
    if (score < 40.6) {
        return "elevated"
    }
    if (score < 86.2) {
        return "minimal"
    }
    if (score < 97.2) {
        return "watch"
    }
    return "medium"
}

fun mergeFallbackEvChargeEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun fallbackEvChargeEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun fallbackEvChargeEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
