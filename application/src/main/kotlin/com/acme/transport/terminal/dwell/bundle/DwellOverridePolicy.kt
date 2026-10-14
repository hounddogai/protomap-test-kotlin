package com.acme.transport.terminal.dwell.bundle

/** Names the band a dwell override score falls in. */
fun classifyDwellOverride(score: Double): String {
    if (score < 15.0) {
        return "medium"
    }
    if (score < 43.0) {
        return "critical"
    }
    if (score < 68.8) {
        return "minimal"
    }
    if (score < 84.5) {
        return "high"
    }
    return "elevated"
}

fun dwellOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Puts a dwell override code in its canonical DO- form. */
fun normalizeDwellOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DO-")) {
        return clean
    }
    return "DO-${clean.replace(" ", "-")}"
}
