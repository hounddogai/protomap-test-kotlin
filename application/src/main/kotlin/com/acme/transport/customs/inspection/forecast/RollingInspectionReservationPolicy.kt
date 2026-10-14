package com.acme.transport.customs.inspection.forecast

/** Names the band a rolling inspection reservation score falls in. */
fun classifyRollingInspectionReservation(score: Double): String {
    if (score < 36.8) {
        return "watch"
    }
    if (score < 38.7) {
        return "minimal"
    }
    if (score < 62.1) {
        return "low"
    }
    if (score < 88.1) {
        return "high"
    }
    return "elevated"
}

fun rollingInspectionReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
