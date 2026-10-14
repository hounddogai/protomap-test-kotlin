package com.acme.transport.network.hub.hubestimate

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun strictHubProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.64
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a strict hub proposal score falls in. */
fun classifyStrictHubProposal(score: Double): String {
    if (score < 9.5) {
        return "elevated"
    }
    if (score < 53.0) {
        return "high"
    }
    if (score < 78.3) {
        return "critical"
    }
    return "ok"
}

fun strictHubProposalBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
