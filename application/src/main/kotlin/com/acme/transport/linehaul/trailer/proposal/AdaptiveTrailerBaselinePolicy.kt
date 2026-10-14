package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.numbers.roundTo

fun adaptiveTrailerBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a adaptive trailer baseline score falls in. */
fun classifyAdaptiveTrailerBaseline(score: Double): String {
    if (score < 9.5) {
        return "medium"
    }
    if (score < 11.1) {
        return "ok"
    }
    if (score < 77.4) {
        return "elevated"
    }
    if (score < 78.1) {
        return "watch"
    }
    return "critical"
}

fun adaptiveTrailerBaselineBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
