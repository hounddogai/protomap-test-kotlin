package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedLanePlanVariance(): Int {
    val policy = defaultPolicy("batched-lane-plan-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 86817.0, in order. */
fun batchedLanePlanVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86817.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
