package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a fallback ev charge record score falls in. */
fun classifyFallbackEvChargeRecord(score: Double): String {
    if (score < 19.5) {
        return "watch"
    }
    if (score < 71.7) {
        return "critical"
    }
    if (score < 81.9) {
        return "minimal"
    }
    return "ok"
}

fun fallbackEvChargeRecordBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun fallbackEvChargeRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeFallbackEvChargeRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
