package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.numbers.roundTo

/** Names the band a manual cross dock audit score falls in. */
fun classifyManualCrossDockAudit(score: Double): String {
    if (score < 57.1) {
        return "low"
    }
    if (score < 100.0) {
        return "high"
    }
    return "minimal"
}

fun manualCrossDockAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun manualCrossDockAuditBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
