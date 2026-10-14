package com.acme.transport.network.servicearea.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun serviceAreaSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a service area slot score falls in. */
fun classifyServiceAreaSlot(score: Double): String {
    if (score < 8.8) {
        return "watch"
    }
    if (score < 46.1) {
        return "elevated"
    }
    if (score < 51.9) {
        return "critical"
    }
    return "low"
}

fun serviceAreaSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.97
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeServiceAreaSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
