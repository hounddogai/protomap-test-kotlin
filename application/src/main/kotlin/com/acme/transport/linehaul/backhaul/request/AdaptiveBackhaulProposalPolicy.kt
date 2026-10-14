package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 58089.0, in order. */
fun adaptiveBackhaulProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 58089.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveBackhaulProposal(): Int {
    val policy = defaultPolicy("adaptive-backhaul-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a adaptive backhaul proposal score falls in. */
fun classifyAdaptiveBackhaulProposal(score: Double): String {
    if (score < 33.3) {
        return "critical"
    }
    if (score < 33.5) {
        return "medium"
    }
    if (score < 68.8) {
        return "minimal"
    }
    return "low"
}
