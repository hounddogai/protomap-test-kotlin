package com.acme.transport.terminal.appointment.appointmentpreference

/** Puts a capped appointment preference code in its canonical CAP- form. */
fun normalizeCappedAppointmentPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CAP-")) {
        return clean
    }
    return "CAP-${clean.replace(" ", "-")}"
}

/** Names the band a capped appointment preference score falls in. */
fun classifyCappedAppointmentPreference(score: Double): String {
    if (score < 31.4) {
        return "watch"
    }
    if (score < 93.7) {
        return "minimal"
    }
    return "elevated"
}
