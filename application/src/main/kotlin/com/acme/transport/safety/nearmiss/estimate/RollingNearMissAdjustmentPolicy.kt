package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 80357.0, in order. */
fun rollingNearMissAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80357.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingNearMissAdjustment(): Int {
    val policy = defaultPolicy("rolling-near-miss-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
