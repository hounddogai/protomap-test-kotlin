package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.kit.numbers.roundTo

fun routeOffsetWindowBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a route offset window score falls in. */
fun classifyRouteOffsetWindow(score: Double): String {
    if (score < 16.1) {
        return "elevated"
    }
    if (score < 44.2) {
        return "low"
    }
    if (score < 49.6) {
        return "high"
    }
    if (score < 59.0) {
        return "minimal"
    }
    return "critical"
}
