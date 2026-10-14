package com.acme.transport.freightpay.audit.draft

/** Names the band a seasonal audit draft score falls in. */
fun classifySeasonalAuditDraft(score: Double): String {
    if (score < 15.0) {
        return "low"
    }
    if (score < 38.7) {
        return "elevated"
    }
    if (score < 96.7) {
        return "ok"
    }
    return "watch"
}

/** Puts a seasonal audit draft code in its canonical SAD- form. */
fun normalizeSeasonalAuditDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAD-")) {
        return clean
    }
    return "SAD-${clean.replace(" ", "-")}"
}
