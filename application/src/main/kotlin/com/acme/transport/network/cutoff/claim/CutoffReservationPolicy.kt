package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.numbers.roundTo

fun cutoffReservationBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun cutoffReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a cutoff reservation score falls in. */
fun classifyCutoffReservation(score: Double): String {
    if (score < 42.9) {
        return "high"
    }
    if (score < 76.5) {
        return "watch"
    }
    if (score < 80.8) {
        return "ok"
    }
    return "critical"
}
