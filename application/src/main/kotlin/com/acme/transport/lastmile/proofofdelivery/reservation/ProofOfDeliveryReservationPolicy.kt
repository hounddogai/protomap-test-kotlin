package com.acme.transport.lastmile.proofofdelivery.reservation

/** Names the band a proof of delivery reservation score falls in. */
fun classifyProofOfDeliveryReservation(score: Double): String {
    if (score < 9.5) {
        return "elevated"
    }
    if (score < 56.7) {
        return "watch"
    }
    if (score < 93.8) {
        return "medium"
    }
    return "critical"
}

fun proofOfDeliveryReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
