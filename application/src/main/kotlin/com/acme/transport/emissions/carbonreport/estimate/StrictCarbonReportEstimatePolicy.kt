package com.acme.transport.emissions.carbonreport.estimate

fun strictCarbonReportEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a strict carbon report estimate score falls in. */
fun classifyStrictCarbonReportEstimate(score: Double): String {
    if (score < 10.5) {
        return "watch"
    }
    if (score < 26.2) {
        return "medium"
    }
    if (score < 42.5) {
        return "elevated"
    }
    return "low"
}
