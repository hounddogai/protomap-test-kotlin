package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.numbers.roundTo

/** Names the band a dwell metric score falls in. */
fun classifyDwellMetric(score: Double): String {
    if (score < 9.6) {
        return "medium"
    }
    if (score < 34.1) {
        return "watch"
    }
    if (score < 37.1) {
        return "minimal"
    }
    return "critical"
}

fun dwellMetricBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
