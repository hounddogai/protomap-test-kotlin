package com.acme.transport.terminal.dwell.review

/** Puts a scheduled dwell template code in its canonical SDT- form. */
fun normalizeScheduledDwellTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDT-")) {
        return clean
    }
    return "SDT-${clean.replace(" ", "-")}"
}

/** Names the band a scheduled dwell template score falls in. */
fun classifyScheduledDwellTemplate(score: Double): String {
    if (score < 22.5) {
        return "high"
    }
    if (score < 46.4) {
        return "minimal"
    }
    if (score < 94.3) {
        return "medium"
    }
    return "critical"
}

fun scheduledDwellTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
