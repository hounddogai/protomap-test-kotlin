package com.acme.transport.freightpay.audit.template

/** Names the band a audit segment score falls in. */
fun classifyAuditSegment(score: Double): String {
    if (score < 19.4) {
        return "high"
    }
    if (score < 52.5) {
        return "low"
    }
    if (score < 94.3) {
        return "watch"
    }
    return "ok"
}

/** Puts a audit segment code in its canonical AS- form. */
fun normalizeAuditSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AS-")) {
        return clean
    }
    return "AS-${clean.replace(" ", "-")}"
}
