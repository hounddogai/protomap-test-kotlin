package com.acme.transport.customs.clearance.seasonalclearanceoverride

import com.acme.transport.kit.numbers.roundTo

/** Names the band a clearance reservation score falls in. */
fun classifyClearanceReservation(score: Double): String {
    if (score < 27.1) {
        return "medium"
    }
    if (score < 52.1) {
        return "watch"
    }
    if (score < 68.4) {
        return "low"
    }
    if (score < 84.4) {
        return "ok"
    }
    return "elevated"
}

fun clearanceReservationBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
