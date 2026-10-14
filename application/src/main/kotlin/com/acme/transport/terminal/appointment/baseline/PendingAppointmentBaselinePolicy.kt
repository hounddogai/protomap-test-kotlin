package com.acme.transport.terminal.appointment.baseline

import com.acme.transport.kit.numbers.roundTo

fun pendingAppointmentBaselineBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun pendingAppointmentBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a pending appointment baseline code in its canonical PAB- form. */
fun normalizePendingAppointmentBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAB-")) {
        return clean
    }
    return "PAB-${clean.replace(" ", "-")}"
}
