package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.kit.numbers.roundTo

fun scorecardAuditBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a scorecard audit score falls in. */
fun classifyScorecardAudit(score: Double): String {
    if (score < 34.2) {
        return "high"
    }
    if (score < 57.4) {
        return "watch"
    }
    if (score < 67.5) {
        return "critical"
    }
    if (score < 90.7) {
        return "ok"
    }
    return "elevated"
}
