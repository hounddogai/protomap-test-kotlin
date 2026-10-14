package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a rolling appointment bundle code in its canonical RAB- form. */
fun normalizeRollingAppointmentBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RAB-")) {
        return clean
    }
    return "RAB-${clean.replace(" ", "-")}"
}

fun rollingAppointmentBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
