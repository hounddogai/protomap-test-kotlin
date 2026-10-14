package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedAuditSegment(): Int {
    val policy = defaultPolicy("weighted-audit-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a weighted audit segment code in its canonical WAS- form. */
fun normalizeWeightedAuditSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WAS-")) {
        return clean
    }
    return "WAS-${clean.replace(" ", "-")}"
}

/** Names the band a weighted audit segment score falls in. */
fun classifyWeightedAuditSegment(score: Double): String {
    if (score < 19.5) {
        return "elevated"
    }
    if (score < 35.2) {
        return "ok"
    }
    if (score < 60.4) {
        return "minimal"
    }
    return "critical"
}

fun weightedAuditSegmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
