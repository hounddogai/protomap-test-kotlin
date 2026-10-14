package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun rollingViolationProposalBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a rolling violation proposal score falls in. */
fun classifyRollingViolationProposal(score: Double): String {
    if (score < 12.3) {
        return "watch"
    }
    if (score < 63.0) {
        return "medium"
    }
    if (score < 82.0) {
        return "elevated"
    }
    if (score < 99.2) {
        return "low"
    }
    return "high"
}

fun rollingViolationProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
