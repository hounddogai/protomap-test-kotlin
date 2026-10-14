package com.acme.transport.crewing.roster.proposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun globalRosterProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a global roster proposal score falls in. */
fun classifyGlobalRosterProposal(score: Double): String {
    if (score < 2.0) {
        return "critical"
    }
    if (score < 4.2) {
        return "minimal"
    }
    if (score < 17.3) {
        return "medium"
    }
    if (score < 47.0) {
        return "high"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalRosterProposal(): Int {
    val policy = defaultPolicy("global-roster-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
