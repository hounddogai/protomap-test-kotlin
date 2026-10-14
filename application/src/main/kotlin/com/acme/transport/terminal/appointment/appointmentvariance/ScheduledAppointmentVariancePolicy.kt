package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a scheduled appointment variance code in its canonical SAV- form. */
fun normalizeScheduledAppointmentVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAV-")) {
        return clean
    }
    return "SAV-${clean.replace(" ", "-")}"
}

fun scheduledAppointmentVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun scheduledAppointmentVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
