package com.acme.transport.crewing.qualification.qualificationmetric

/** Names the band a capped qualification summary score falls in. */
fun classifyCappedQualificationSummary(score: Double): String {
    if (score < 7.9) {
        return "high"
    }
    if (score < 35.0) {
        return "minimal"
    }
    if (score < 44.8) {
        return "elevated"
    }
    if (score < 63.9) {
        return "watch"
    }
    return "critical"
}

fun cappedQualificationSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
