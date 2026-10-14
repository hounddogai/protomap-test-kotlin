package com.acme.transport.linehaul.relay.summary

/** Names the band a relay summary score falls in. */
fun classifyRelaySummary(score: Double): String {
    if (score < 14.1) {
        return "minimal"
    }
    if (score < 15.1) {
        return "high"
    }
    if (score < 17.3) {
        return "medium"
    }
    if (score < 55.0) {
        return "elevated"
    }
    return "ok"
}

/** Puts a relay summary code in its canonical RS- form. */
fun normalizeRelaySummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RS-")) {
        return clean
    }
    return "RS-${clean.replace(" ", "-")}"
}
