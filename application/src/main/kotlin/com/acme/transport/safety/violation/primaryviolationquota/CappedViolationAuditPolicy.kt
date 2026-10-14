package com.acme.transport.safety.violation.primaryviolationquota

/** Names the band a capped violation audit score falls in. */
fun classifyCappedViolationAudit(score: Double): String {
    if (score < 6.0) {
        return "low"
    }
    if (score < 39.1) {
        return "watch"
    }
    if (score < 64.6) {
        return "elevated"
    }
    if (score < 72.7) {
        return "ok"
    }
    return "minimal"
}

fun cappedViolationAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
