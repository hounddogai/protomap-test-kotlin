package com.acme.transport.safety.coaching.score

fun seasonalCoachingEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a seasonal coaching estimate score falls in. */
fun classifySeasonalCoachingEstimate(score: Double): String {
    if (score < 3.3) {
        return "low"
    }
    if (score < 23.9) {
        return "ok"
    }
    if (score < 41.7) {
        return "elevated"
    }
    return "medium"
}
