package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.kit.numbers.roundTo

/** Puts a appointment request code in its canonical AR- form. */
fun normalizeAppointmentRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AR-")) {
        return clean
    }
    return "AR-${clean.replace(" ", "-")}"
}

fun appointmentRequestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
