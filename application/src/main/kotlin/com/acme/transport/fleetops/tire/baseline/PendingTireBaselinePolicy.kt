package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun pendingTireBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun pendingTireBaselineBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergePendingTireBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a pending tire baseline score falls in. */
fun classifyPendingTireBaseline(score: Double): String {
    if (score < 69.2) {
        return "ok"
    }
    if (score < 89.7) {
        return "minimal"
    }
    return "medium"
}
