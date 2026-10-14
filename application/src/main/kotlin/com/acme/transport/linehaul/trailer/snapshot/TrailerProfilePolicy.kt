package com.acme.transport.linehaul.trailer.snapshot

/** Names the band a trailer profile score falls in. */
fun classifyTrailerProfile(score: Double): String {
    if (score < 18.5) {
        return "ok"
    }
    if (score < 20.8) {
        return "minimal"
    }
    return "low"
}

/** Puts a trailer profile code in its canonical TP- form. */
fun normalizeTrailerProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TP-")) {
        return clean
    }
    return "TP-${clean.replace(" ", "-")}"
}
