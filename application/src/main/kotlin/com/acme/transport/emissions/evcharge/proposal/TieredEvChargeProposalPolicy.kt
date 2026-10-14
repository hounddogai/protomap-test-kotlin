package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tieredEvChargeProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a tiered ev charge proposal score falls in. */
fun classifyTieredEvChargeProposal(score: Double): String {
    if (score < 11.3) {
        return "ok"
    }
    if (score < 81.3) {
        return "minimal"
    }
    return "watch"
}

fun tieredEvChargeProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.22
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
