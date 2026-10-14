package com.acme.transport.lastmile.drop.profile

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun adaptiveDropRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeAdaptiveDropRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a adaptive drop record score falls in. */
fun classifyAdaptiveDropRecord(score: Double): String {
    if (score < 33.6) {
        return "minimal"
    }
    if (score < 49.6) {
        return "critical"
    }
    if (score < 64.4) {
        return "ok"
    }
    if (score < 96.2) {
        return "elevated"
    }
    return "medium"
}

fun adaptiveDropRecordBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
