package com.acme.transport.safety.coaching.exception

/** Names the band a capped coaching reservation score falls in. */
fun classifyCappedCoachingReservation(score: Double): String {
    if (score < 51.5) {
        return "minimal"
    }
    if (score < 94.0) {
        return "elevated"
    }
    return "watch"
}

/** Puts a capped coaching reservation code in its canonical CCR- form. */
fun normalizeCappedCoachingReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CCR-")) {
        return clean
    }
    return "CCR-${clean.replace(" ", "-")}"
}
