package com.acme.transport.freightpay.audit.claim

import com.acme.transport.kit.numbers.roundTo

/** Names the band a weighted audit claim score falls in. */
fun classifyWeightedAuditClaim(score: Double): String {
    if (score < 32.5) {
        return "critical"
    }
    if (score < 70.1) {
        return "low"
    }
    if (score < 73.2) {
        return "ok"
    }
    if (score < 93.7) {
        return "watch"
    }
    return "high"
}

/** Puts a weighted audit claim code in its canonical WAC- form. */
fun normalizeWeightedAuditClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WAC-")) {
        return clean
    }
    return "WAC-${clean.replace(" ", "-")}"
}

fun weightedAuditClaimBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
