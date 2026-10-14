package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.numbers.roundTo

fun carbonReportDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a carbon report draft score falls in. */
fun classifyCarbonReportDraft(score: Double): String {
    if (score < 25.2) {
        return "medium"
    }
    if (score < 28.9) {
        return "critical"
    }
    if (score < 30.5) {
        return "minimal"
    }
    if (score < 43.0) {
        return "ok"
    }
    return "watch"
}

fun carbonReportDraftBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
