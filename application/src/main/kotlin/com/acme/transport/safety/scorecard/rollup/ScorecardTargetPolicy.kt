package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.numbers.roundTo

fun scorecardTargetBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a scorecard target score falls in. */
fun classifyScorecardTarget(score: Double): String {
    if (score < 9.8) {
        return "minimal"
    }
    if (score < 61.7) {
        return "elevated"
    }
    if (score < 85.4) {
        return "critical"
    }
    return "high"
}
