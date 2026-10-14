package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.numbers.roundTo

fun manualScorecardAuditBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun manualScorecardAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a manual scorecard audit score falls in. */
fun classifyManualScorecardAudit(score: Double): String {
    if (score < 4.6) {
        return "medium"
    }
    if (score < 47.4) {
        return "high"
    }
    if (score < 54.1) {
        return "watch"
    }
    if (score < 99.9) {
        return "elevated"
    }
    return "low"
}
