package com.acme.transport.lastmile.redelivery.ledger

/** Names the band a lenient redelivery audit score falls in. */
fun classifyLenientRedeliveryAudit(score: Double): String {
    if (score < 56.1) {
        return "low"
    }
    if (score < 88.1) {
        return "critical"
    }
    return "minimal"
}

fun lenientRedeliveryAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
