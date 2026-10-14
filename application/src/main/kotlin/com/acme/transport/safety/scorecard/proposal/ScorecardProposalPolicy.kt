package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.kit.numbers.roundTo

fun scorecardProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a scorecard proposal score falls in. */
fun classifyScorecardProposal(score: Double): String {
    if (score < 30.5) {
        return "critical"
    }
    if (score < 73.7) {
        return "minimal"
    }
    if (score < 79.9) {
        return "low"
    }
    if (score < 96.1) {
        return "ok"
    }
    return "watch"
}

fun scorecardProposalBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
