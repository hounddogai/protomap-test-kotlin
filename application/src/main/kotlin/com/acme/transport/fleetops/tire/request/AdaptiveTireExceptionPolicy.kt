package com.acme.transport.fleetops.tire.request

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 13372.0, in order. */
fun adaptiveTireExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 13372.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveTireException(): Int {
    val policy = defaultPolicy("adaptive-tire-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
