package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a pending appointment audit code in its canonical PAA- form. */
fun normalizePendingAppointmentAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAA-")) {
        return clean
    }
    return "PAA-${clean.replace(" ", "-")}"
}

fun pendingAppointmentAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.16
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
