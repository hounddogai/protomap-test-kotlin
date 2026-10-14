package com.acme.transport.network.node.nodecycle

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackNodeCycle(): Int {
    val policy = defaultPolicy("fallback-node-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 21788.0, in order. */
fun fallbackNodeCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21788.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
