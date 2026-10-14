package com.acme.transport.crewing.roster.rostercycle

/** Puts a scheduled roster override code in its canonical SRO- form. */
fun normalizeScheduledRosterOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRO-")) {
        return clean
    }
    return "SRO-${clean.replace(" ", "-")}"
}

/** Names the band a scheduled roster override score falls in. */
fun classifyScheduledRosterOverride(score: Double): String {
    if (score < 37.1) {
        return "medium"
    }
    if (score < 58.2) {
        return "high"
    }
    if (score < 80.9) {
        return "minimal"
    }
    if (score < 81.6) {
        return "watch"
    }
    return "low"
}

fun scheduledRosterOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
