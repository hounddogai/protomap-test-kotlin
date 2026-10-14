package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.numbers.roundTo

/** Names the band a lenient route offset estimate score falls in. */
fun classifyLenientRouteOffsetEstimate(score: Double): String {
    if (score < 29.0) {
        return "ok"
    }
    if (score < 58.1) {
        return "critical"
    }
    if (score < 74.1) {
        return "low"
    }
    if (score < 97.2) {
        return "elevated"
    }
    return "high"
}

fun lenientRouteOffsetEstimateBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
