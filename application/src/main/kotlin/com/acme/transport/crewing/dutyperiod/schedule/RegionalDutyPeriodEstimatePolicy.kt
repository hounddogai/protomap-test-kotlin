package com.acme.transport.crewing.dutyperiod.schedule

fun regionalDutyPeriodEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a regional duty period estimate score falls in. */
fun classifyRegionalDutyPeriodEstimate(score: Double): String {
    if (score < 52.1) {
        return "critical"
    }
    if (score < 64.7) {
        return "elevated"
    }
    if (score < 72.5) {
        return "ok"
    }
    if (score < 93.0) {
        return "medium"
    }
    return "minimal"
}
