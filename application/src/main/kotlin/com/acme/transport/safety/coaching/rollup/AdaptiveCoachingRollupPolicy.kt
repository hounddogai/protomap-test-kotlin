package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun adaptiveCoachingRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun mergeAdaptiveCoachingRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a adaptive coaching rollup score falls in. */
fun classifyAdaptiveCoachingRollup(score: Double): String {
    if (score < 37.8) {
        return "elevated"
    }
    if (score < 70.1) {
        return "critical"
    }
    if (score < 74.7) {
        return "low"
    }
    if (score < 89.9) {
        return "high"
    }
    return "watch"
}
