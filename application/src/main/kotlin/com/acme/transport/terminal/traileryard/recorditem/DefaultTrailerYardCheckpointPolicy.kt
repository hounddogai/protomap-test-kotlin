package com.acme.transport.terminal.traileryard.recorditem

/** Puts a default trailer yard checkpoint code in its canonical DTYC- form. */
fun normalizeDefaultTrailerYardCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DTYC-")) {
        return clean
    }
    return "DTYC-${clean.replace(" ", "-")}"
}

/** Names the band a default trailer yard checkpoint score falls in. */
fun classifyDefaultTrailerYardCheckpoint(score: Double): String {
    if (score < 6.8) {
        return "low"
    }
    if (score < 8.5) {
        return "elevated"
    }
    if (score < 8.6) {
        return "medium"
    }
    if (score < 39.5) {
        return "watch"
    }
    return "minimal"
}
