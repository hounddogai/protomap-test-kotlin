package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a coaching proposal score falls in. */
fun classifyCoachingProposal(score: Double): String {
    if (score < 11.8) {
        return "critical"
    }
    if (score < 41.5) {
        return "ok"
    }
    if (score < 75.0) {
        return "elevated"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCoachingProposal(): Int {
    val policy = defaultPolicy("coaching-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 70930.0, in order. */
fun coachingProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70930.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
