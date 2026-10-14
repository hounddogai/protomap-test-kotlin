package com.acme.transport.terminal.appointment.appointmentmapping

/** Names the band a scheduled appointment mapping score falls in. */
fun classifyScheduledAppointmentMapping(score: Double): String {
    if (score < 6.7) {
        return "critical"
    }
    if (score < 40.7) {
        return "ok"
    }
    if (score < 50.1) {
        return "minimal"
    }
    if (score < 87.6) {
        return "low"
    }
    return "medium"
}

/** Puts a scheduled appointment mapping code in its canonical SAM- form. */
fun normalizeScheduledAppointmentMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAM-")) {
        return clean
    }
    return "SAM-${clean.replace(" ", "-")}"
}
