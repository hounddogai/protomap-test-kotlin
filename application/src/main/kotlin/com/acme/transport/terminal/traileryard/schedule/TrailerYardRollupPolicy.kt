package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.numbers.roundTo

fun trailerYardRollupBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun trailerYardRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a trailer yard rollup score falls in. */
fun classifyTrailerYardRollup(score: Double): String {
    if (score < 4.2) {
        return "watch"
    }
    if (score < 35.0) {
        return "minimal"
    }
    if (score < 45.1) {
        return "high"
    }
    if (score < 76.2) {
        return "elevated"
    }
    return "critical"
}
