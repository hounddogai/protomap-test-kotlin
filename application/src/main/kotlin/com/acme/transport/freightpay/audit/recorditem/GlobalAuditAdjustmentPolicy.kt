package com.acme.transport.freightpay.audit.recorditem

import com.acme.transport.kit.numbers.roundTo

/** Names the band a global audit adjustment score falls in. */
fun classifyGlobalAuditAdjustment(score: Double): String {
    if (score < 54.2) {
        return "high"
    }
    if (score < 77.1) {
        return "ok"
    }
    return "minimal"
}

fun globalAuditAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun globalAuditAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
