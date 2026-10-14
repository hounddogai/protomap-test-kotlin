package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 50229.0, in order. */
fun adaptiveNearMissVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 50229.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveNearMissVariance(): Int {
    val policy = defaultPolicy("adaptive-near-miss-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
