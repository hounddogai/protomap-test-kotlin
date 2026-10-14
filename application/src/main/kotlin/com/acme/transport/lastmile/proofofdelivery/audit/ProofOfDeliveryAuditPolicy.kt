package com.acme.transport.lastmile.proofofdelivery.audit

fun proofOfDeliveryAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a proof of delivery audit score falls in. */
fun classifyProofOfDeliveryAudit(score: Double): String {
    if (score < 47.5) {
        return "watch"
    }
    if (score < 91.5) {
        return "minimal"
    }
    if (score < 94.1) {
        return "ok"
    }
    return "critical"
}

/** Puts a proof of delivery audit code in its canonical PODA- form. */
fun normalizeProofOfDeliveryAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODA-")) {
        return clean
    }
    return "PODA-${clean.replace(" ", "-")}"
}
