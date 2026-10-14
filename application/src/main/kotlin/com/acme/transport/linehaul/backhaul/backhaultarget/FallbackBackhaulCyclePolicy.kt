package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun fallbackBackhaulCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fallback backhaul cycle score falls in. */
fun classifyFallbackBackhaulCycle(score: Double): String {
    if (score < 38.0) {
        return "medium"
    }
    if (score < 66.3) {
        return "critical"
    }
    if (score < 79.7) {
        return "minimal"
    }
    if (score < 92.1) {
        return "watch"
    }
    return "low"
}

fun mergeFallbackBackhaulCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun fallbackBackhaulCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
