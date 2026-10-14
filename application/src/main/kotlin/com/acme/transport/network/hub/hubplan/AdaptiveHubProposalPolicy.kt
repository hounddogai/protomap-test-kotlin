package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun adaptiveHubProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.82
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun adaptiveHubProposalBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a adaptive hub proposal score falls in. */
fun classifyAdaptiveHubProposal(score: Double): String {
    if (score < 36.1) {
        return "ok"
    }
    if (score < 55.8) {
        return "minimal"
    }
    return "critical"
}
