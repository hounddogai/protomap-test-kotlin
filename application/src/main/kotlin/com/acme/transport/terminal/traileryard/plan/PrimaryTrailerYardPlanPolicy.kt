package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryTrailerYardPlan(): Int {
    val policy = defaultPolicy("primary-trailer-yard-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a primary trailer yard plan score falls in. */
fun classifyPrimaryTrailerYardPlan(score: Double): String {
    if (score < 37.6) {
        return "high"
    }
    if (score < 55.1) {
        return "low"
    }
    if (score < 82.9) {
        return "ok"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 7323.0, in order. */
fun primaryTrailerYardPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7323.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
