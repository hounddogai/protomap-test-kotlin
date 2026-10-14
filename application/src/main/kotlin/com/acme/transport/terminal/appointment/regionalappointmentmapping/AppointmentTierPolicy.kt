package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.kit.numbers.roundTo

/** Puts a appointment tier code in its canonical AT- form. */
fun normalizeAppointmentTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AT-")) {
        return clean
    }
    return "AT-${clean.replace(" ", "-")}"
}

fun appointmentTierBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
