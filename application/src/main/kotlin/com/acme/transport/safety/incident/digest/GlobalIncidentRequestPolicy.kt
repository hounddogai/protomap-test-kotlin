package com.acme.transport.safety.incident.digest

/** Names the band a global incident request score falls in. */
fun classifyGlobalIncidentRequest(score: Double): String {
    if (score < 16.8) {
        return "elevated"
    }
    if (score < 47.4) {
        return "low"
    }
    if (score < 69.8) {
        return "minimal"
    }
    return "watch"
}

/** Puts a global incident request code in its canonical GIR- form. */
fun normalizeGlobalIncidentRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GIR-")) {
        return clean
    }
    return "GIR-${clean.replace(" ", "-")}"
}
