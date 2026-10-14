package com.acme.transport.terminal.dwell.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a dwell batch score falls in. */
fun classifyDwellBatch(score: Double): String {
    if (score < 20.0) {
        return "medium"
    }
    if (score < 39.2) {
        return "minimal"
    }
    if (score < 40.6) {
        return "low"
    }
    if (score < 90.5) {
        return "elevated"
    }
    return "ok"
}

fun dwellBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.93
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeDwellBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun dwellBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
