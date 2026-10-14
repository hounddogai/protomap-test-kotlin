package com.acme.transport.customs.hold.audit

import com.acme.transport.kit.numbers.roundTo

fun pendingHoldAuditBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a pending hold audit score falls in. */
fun classifyPendingHoldAudit(score: Double): String {
    if (score < 36.5) {
        return "minimal"
    }
    if (score < 57.5) {
        return "elevated"
    }
    if (score < 62.5) {
        return "ok"
    }
    if (score < 71.3) {
        return "critical"
    }
    return "high"
}
