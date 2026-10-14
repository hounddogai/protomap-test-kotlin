package com.acme.transport.linehaul.trailer.proposal

/** Names the band a trailer audit score falls in. */
fun classifyTrailerAudit(score: Double): String {
    if (score < 44.1) {
        return "medium"
    }
    if (score < 47.4) {
        return "critical"
    }
    if (score < 53.2) {
        return "high"
    }
    if (score < 71.6) {
        return "ok"
    }
    return "watch"
}

fun trailerAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
