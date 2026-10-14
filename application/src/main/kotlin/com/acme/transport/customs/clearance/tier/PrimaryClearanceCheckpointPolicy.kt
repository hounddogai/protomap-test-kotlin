package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a primary clearance checkpoint score falls in. */
fun classifyPrimaryClearanceCheckpoint(score: Double): String {
    if (score < 47.2) {
        return "watch"
    }
    if (score < 78.2) {
        return "ok"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 7098.0, in order. */
fun primaryClearanceCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7098.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryClearanceCheckpoint(): Int {
    val policy = defaultPolicy("primary-clearance-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
