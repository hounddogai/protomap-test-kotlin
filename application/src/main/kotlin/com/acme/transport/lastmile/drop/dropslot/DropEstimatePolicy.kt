package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeDropEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a drop estimate score falls in. */
fun classifyDropEstimate(score: Double): String {
    if (score < 25.1) {
        return "high"
    }
    if (score < 32.3) {
        return "minimal"
    }
    if (score < 46.3) {
        return "critical"
    }
    if (score < 48.6) {
        return "ok"
    }
    return "low"
}

fun dropEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun dropEstimateBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
