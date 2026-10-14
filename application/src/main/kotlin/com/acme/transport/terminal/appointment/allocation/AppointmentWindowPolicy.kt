package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.numbers.roundTo

fun appointmentWindowBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a appointment window score falls in. */
fun classifyAppointmentWindow(score: Double): String {
    if (score < 63.3) {
        return "watch"
    }
    if (score < 69.5) {
        return "minimal"
    }
    return "ok"
}
