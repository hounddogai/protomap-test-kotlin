package com.acme.transport.lastmile.drop.cappeddropmilestone

/** Names the band a lenient drop reservation score falls in. */
fun classifyLenientDropReservation(score: Double): String {
    if (score < 13.9) {
        return "medium"
    }
    if (score < 89.9) {
        return "critical"
    }
    if (score < 97.2) {
        return "elevated"
    }
    return "ok"
}

/** Puts a lenient drop reservation code in its canonical LDR- form. */
fun normalizeLenientDropReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDR-")) {
        return clean
    }
    return "LDR-${clean.replace(" ", "-")}"
}
