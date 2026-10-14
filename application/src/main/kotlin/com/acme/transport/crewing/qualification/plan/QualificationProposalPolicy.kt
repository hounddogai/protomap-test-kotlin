package com.acme.transport.crewing.qualification.plan

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 4340.0, in order. */
fun qualificationProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4340.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeQualificationProposal(): Int {
    val policy = defaultPolicy("qualification-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a qualification proposal score falls in. */
fun classifyQualificationProposal(score: Double): String {
    if (score < 13.4) {
        return "watch"
    }
    if (score < 32.4) {
        return "critical"
    }
    if (score < 78.8) {
        return "minimal"
    }
    if (score < 96.1) {
        return "high"
    }
    return "ok"
}

fun qualificationProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
