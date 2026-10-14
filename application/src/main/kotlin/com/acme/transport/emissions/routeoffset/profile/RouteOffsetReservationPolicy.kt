package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.numbers.roundTo

fun routeOffsetReservationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun routeOffsetReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a route offset reservation score falls in. */
fun classifyRouteOffsetReservation(score: Double): String {
    if (score < 8.3) {
        return "low"
    }
    if (score < 69.1) {
        return "watch"
    }
    return "high"
}
