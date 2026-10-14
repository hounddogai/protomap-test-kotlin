package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 76322.0, in order. */
fun relayScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76322.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRelayScore(): Int {
    val policy = defaultPolicy("relay-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
