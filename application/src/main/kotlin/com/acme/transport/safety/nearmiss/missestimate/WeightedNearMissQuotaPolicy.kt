package com.acme.transport.safety.nearmiss.missestimate

fun weightedNearMissQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a weighted near miss quota score falls in. */
fun classifyWeightedNearMissQuota(score: Double): String {
    if (score < 3.2) {
        return "minimal"
    }
    if (score < 35.7) {
        return "medium"
    }
    if (score < 48.2) {
        return "low"
    }
    if (score < 94.1) {
        return "elevated"
    }
    return "high"
}
