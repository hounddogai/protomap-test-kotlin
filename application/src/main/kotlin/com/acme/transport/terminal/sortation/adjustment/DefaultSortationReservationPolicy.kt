package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.numbers.roundTo

fun defaultSortationReservationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun defaultSortationReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a default sortation reservation score falls in. */
fun classifyDefaultSortationReservation(score: Double): String {
    if (score < 10.2) {
        return "elevated"
    }
    if (score < 70.6) {
        return "high"
    }
    if (score < 79.7) {
        return "watch"
    }
    if (score < 91.5) {
        return "medium"
    }
    return "critical"
}
