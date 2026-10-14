package com.acme.transport.freightpay.audit.auditreview

/** Names the band a batched audit quota score falls in. */
fun classifyBatchedAuditQuota(score: Double): String {
    if (score < 63.3) {
        return "ok"
    }
    if (score < 66.5) {
        return "low"
    }
    return "medium"
}

/** Puts a batched audit quota code in its canonical BAQ- form. */
fun normalizeBatchedAuditQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BAQ-")) {
        return clean
    }
    return "BAQ-${clean.replace(" ", "-")}"
}
