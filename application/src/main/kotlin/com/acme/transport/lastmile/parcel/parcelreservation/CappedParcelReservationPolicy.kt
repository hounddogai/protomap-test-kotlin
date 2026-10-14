package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.numbers.roundTo

fun cappedParcelReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun cappedParcelReservationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a capped parcel reservation score falls in. */
fun classifyCappedParcelReservation(score: Double): String {
    if (score < 9.2) {
        return "elevated"
    }
    if (score < 58.1) {
        return "high"
    }
    if (score < 79.1) {
        return "low"
    }
    return "medium"
}
