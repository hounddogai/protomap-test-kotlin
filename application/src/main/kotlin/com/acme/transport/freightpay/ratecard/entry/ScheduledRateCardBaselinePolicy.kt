package com.acme.transport.freightpay.ratecard.entry

/** Names the band a scheduled rate card baseline score falls in. */
fun classifyScheduledRateCardBaseline(score: Double): String {
    if (score < 21.3) {
        return "low"
    }
    if (score < 49.2) {
        return "elevated"
    }
    if (score < 98.0) {
        return "high"
    }
    return "ok"
}

/** Puts a scheduled rate card baseline code in its canonical SRCB- form. */
fun normalizeScheduledRateCardBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRCB-")) {
        return clean
    }
    return "SRCB-${clean.replace(" ", "-")}"
}

fun scheduledRateCardBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
