package com.acme.transport.crewing.restbreak.legacybreakentry

/** Puts a rolling rest break entry code in its canonical RRBE- form. */
fun normalizeRollingRestBreakEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRBE-")) {
        return clean
    }
    return "RRBE-${clean.replace(" ", "-")}"
}

fun rollingRestBreakEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a rolling rest break entry score falls in. */
fun classifyRollingRestBreakEntry(score: Double): String {
    if (score < 62.4) {
        return "elevated"
    }
    if (score < 68.8) {
        return "ok"
    }
    return "critical"
}
