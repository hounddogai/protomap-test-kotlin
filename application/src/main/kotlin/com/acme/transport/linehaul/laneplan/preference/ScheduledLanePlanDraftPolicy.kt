package com.acme.transport.linehaul.laneplan.preference

/** Puts a scheduled lane plan draft code in its canonical SLPD- form. */
fun normalizeScheduledLanePlanDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SLPD-")) {
        return clean
    }
    return "SLPD-${clean.replace(" ", "-")}"
}

/** Names the band a scheduled lane plan draft score falls in. */
fun classifyScheduledLanePlanDraft(score: Double): String {
    if (score < 62.6) {
        return "ok"
    }
    if (score < 73.3) {
        return "watch"
    }
    return "minimal"
}

fun scheduledLanePlanDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
