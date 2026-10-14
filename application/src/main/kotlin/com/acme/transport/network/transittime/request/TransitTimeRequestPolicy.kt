package com.acme.transport.network.transittime.request

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTransitTimeRequest(): Int {
    val policy = defaultPolicy("transit-time-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 20612.0, in order. */
fun transitTimeRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20612.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
