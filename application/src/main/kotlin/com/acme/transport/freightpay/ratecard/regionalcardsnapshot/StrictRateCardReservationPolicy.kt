package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.kit.numbers.roundTo

fun strictRateCardReservationBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a strict rate card reservation score falls in. */
fun classifyStrictRateCardReservation(score: Double): String {
    if (score < 16.1) {
        return "low"
    }
    if (score < 16.9) {
        return "elevated"
    }
    if (score < 48.1) {
        return "minimal"
    }
    if (score < 80.4) {
        return "ok"
    }
    return "medium"
}
