package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a legacy audit record code in its canonical LAR- form. */
fun normalizeLegacyAuditRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LAR-")) {
        return clean
    }
    return "LAR-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyAuditRecord(): Int {
    val policy = defaultPolicy("legacy-audit-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun legacyAuditRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a legacy audit record score falls in. */
fun classifyLegacyAuditRecord(score: Double): String {
    if (score < 14.4) {
        return "watch"
    }
    if (score < 39.2) {
        return "critical"
    }
    if (score < 46.6) {
        return "high"
    }
    if (score < 95.9) {
        return "minimal"
    }
    return "elevated"
}
