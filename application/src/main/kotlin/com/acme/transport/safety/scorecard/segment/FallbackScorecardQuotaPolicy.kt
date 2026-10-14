package com.acme.transport.safety.scorecard.segment

fun fallbackScorecardQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Names the band a fallback scorecard quota score falls in. */
fun classifyFallbackScorecardQuota(score: Double): String {
    if (score < 1.2) {
        return "watch"
    }
    if (score < 92.8) {
        return "low"
    }
    return "critical"
}
