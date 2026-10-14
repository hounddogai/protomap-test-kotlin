package com.acme.transport.network.hub.hubentry

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHubBundle(): Int {
    val policy = defaultPolicy("hub-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 77095.0, in order. */
fun hubBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77095.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
