package com.acme.transport.terminal.appointment.group

fun fallbackAppointmentGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a fallback appointment group code in its canonical FAG- form. */
fun normalizeFallbackAppointmentGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FAG-")) {
        return clean
    }
    return "FAG-${clean.replace(" ", "-")}"
}
