package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a seasonal coaching metric score falls in. */
fun classifySeasonalCoachingMetric(score: Double): String {
    if (score < 47.5) {
        return "medium"
    }
    if (score < 49.4) {
        return "high"
    }
    if (score < 86.6) {
        return "minimal"
    }
    return "low"
}

fun mergeSeasonalCoachingMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun seasonalCoachingMetricBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun seasonalCoachingMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
