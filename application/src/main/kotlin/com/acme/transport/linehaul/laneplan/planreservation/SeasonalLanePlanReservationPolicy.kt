package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.kit.numbers.roundTo

fun seasonalLanePlanReservationBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a seasonal lane plan reservation score falls in. */
fun classifySeasonalLanePlanReservation(score: Double): String {
    if (score < 35.8) {
        return "low"
    }
    if (score < 66.1) {
        return "watch"
    }
    if (score < 99.5) {
        return "high"
    }
    return "ok"
}

fun seasonalLanePlanReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
