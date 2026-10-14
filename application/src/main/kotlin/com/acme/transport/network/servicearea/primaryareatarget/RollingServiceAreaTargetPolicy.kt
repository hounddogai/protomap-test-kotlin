package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeRollingServiceAreaTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun rollingServiceAreaTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a rolling service area target score falls in. */
fun classifyRollingServiceAreaTarget(score: Double): String {
    if (score < 38.3) {
        return "high"
    }
    if (score < 47.8) {
        return "watch"
    }
    if (score < 93.3) {
        return "medium"
    }
    return "critical"
}

fun rollingServiceAreaTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
