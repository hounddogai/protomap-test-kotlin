package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a capped near miss proposal score falls in. */
fun classifyCappedNearMissProposal(score: Double): String {
    if (score < 50.4) {
        return "high"
    }
    if (score < 50.9) {
        return "minimal"
    }
    if (score < 90.8) {
        return "watch"
    }
    if (score < 92.4) {
        return "medium"
    }
    return "elevated"
}

fun cappedNearMissProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.72
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
