package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.numbers.roundTo

fun batchedRosterAgreementBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a batched roster agreement score falls in. */
fun classifyBatchedRosterAgreement(score: Double): String {
    if (score < 51.5) {
        return "high"
    }
    if (score < 92.1) {
        return "low"
    }
    return "ok"
}

/** Puts a batched roster agreement code in its canonical BRA- form. */
fun normalizeBatchedRosterAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BRA-")) {
        return clean
    }
    return "BRA-${clean.replace(" ", "-")}"
}
