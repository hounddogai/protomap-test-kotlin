package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeClearanceScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun clearanceScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a clearance score score falls in. */
fun classifyClearanceScore(score: Double): String {
    if (score < 24.8) {
        return "minimal"
    }
    if (score < 26.3) {
        return "high"
    }
    if (score < 89.3) {
        return "critical"
    }
    return "medium"
}

fun clearanceScoreBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
