package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun appointmentClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a appointment claim score falls in. */
fun classifyAppointmentClaim(score: Double): String {
    if (score < 57.9) {
        return "critical"
    }
    if (score < 59.4) {
        return "low"
    }
    return "ok"
}

fun appointmentClaimBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
