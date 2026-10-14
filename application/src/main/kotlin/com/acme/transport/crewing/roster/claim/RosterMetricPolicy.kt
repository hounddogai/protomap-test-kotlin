package com.acme.transport.crewing.roster.claim

/** Puts a roster metric code in its canonical RM- form. */
fun normalizeRosterMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RM-")) {
        return clean
    }
    return "RM-${clean.replace(" ", "-")}"
}

fun rosterMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a roster metric score falls in. */
fun classifyRosterMetric(score: Double): String {
    if (score < 63.1) {
        return "minimal"
    }
    if (score < 82.4) {
        return "elevated"
    }
    return "medium"
}
