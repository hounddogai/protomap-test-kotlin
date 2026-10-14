package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a zone reservation score falls in. */
fun classifyZoneReservation(score: Double): String {
    if (score < 13.8) {
        return "watch"
    }
    if (score < 15.2) {
        return "critical"
    }
    if (score < 56.3) {
        return "elevated"
    }
    if (score < 85.9) {
        return "low"
    }
    return "ok"
}

fun zoneReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
