package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.numbers.roundTo

fun auditManifestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a audit manifest code in its canonical AM- form. */
fun normalizeAuditManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AM-")) {
        return clean
    }
    return "AM-${clean.replace(" ", "-")}"
}

/** Names the band a audit manifest score falls in. */
fun classifyAuditManifest(score: Double): String {
    if (score < 5.4) {
        return "medium"
    }
    if (score < 73.7) {
        return "elevated"
    }
    return "minimal"
}
