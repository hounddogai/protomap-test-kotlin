package com.acme.transport.crewing.qualification.bundle

fun pendingQualificationEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a pending qualification estimate score falls in. */
fun classifyPendingQualificationEstimate(score: Double): String {
    if (score < 54.0) {
        return "critical"
    }
    if (score < 68.1) {
        return "watch"
    }
    if (score < 99.8) {
        return "low"
    }
    return "high"
}
