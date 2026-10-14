package com.acme.transport.customs.clearance.policy

fun clearanceAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a clearance audit score falls in. */
fun classifyClearanceAudit(score: Double): String {
    if (score < 5.0) {
        return "watch"
    }
    if (score < 15.1) {
        return "high"
    }
    return "ok"
}
