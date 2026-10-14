package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeAdaptiveEvChargeWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a adaptive ev charge window score falls in. */
fun classifyAdaptiveEvChargeWindow(score: Double): String {
    if (score < 24.8) {
        return "low"
    }
    if (score < 69.1) {
        return "high"
    }
    if (score < 89.1) {
        return "medium"
    }
    if (score < 90.9) {
        return "critical"
    }
    return "minimal"
}

fun adaptiveEvChargeWindowBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun adaptiveEvChargeWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
