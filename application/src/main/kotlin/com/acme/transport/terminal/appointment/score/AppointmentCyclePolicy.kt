package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a appointment cycle code in its canonical AC- form. */
fun normalizeAppointmentCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AC-")) {
        return clean
    }
    return "AC-${clean.replace(" ", "-")}"
}

fun appointmentCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.4
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
