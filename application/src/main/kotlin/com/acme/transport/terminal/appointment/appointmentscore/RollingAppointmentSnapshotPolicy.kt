package com.acme.transport.terminal.appointment.appointmentscore

/** Puts a rolling appointment snapshot code in its canonical RAS- form. */
fun normalizeRollingAppointmentSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RAS-")) {
        return clean
    }
    return "RAS-${clean.replace(" ", "-")}"
}

fun rollingAppointmentSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
