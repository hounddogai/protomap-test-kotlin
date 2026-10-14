package com.acme.transport.freightpay.audit.claim

/** Names the band a audit checkpoint score falls in. */
fun classifyAuditCheckpoint(score: Double): String {
    if (score < 66.8) {
        return "minimal"
    }
    if (score < 68.0) {
        return "ok"
    }
    if (score < 91.9) {
        return "watch"
    }
    return "elevated"
}

/** Puts a audit checkpoint code in its canonical AC- form. */
fun normalizeAuditCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AC-")) {
        return clean
    }
    return "AC-${clean.replace(" ", "-")}"
}
