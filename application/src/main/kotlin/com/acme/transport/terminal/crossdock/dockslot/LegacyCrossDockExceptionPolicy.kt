package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 87740.0, in order. */
fun legacyCrossDockExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 87740.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyCrossDockException(): Int {
    val policy = defaultPolicy("legacy-cross-dock-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
