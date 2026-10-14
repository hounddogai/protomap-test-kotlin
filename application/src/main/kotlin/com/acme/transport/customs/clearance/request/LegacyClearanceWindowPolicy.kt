package com.acme.transport.customs.clearance.request

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 32476.0, in order. */
fun legacyClearanceWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32476.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyClearanceWindow(): Int {
    val policy = defaultPolicy("legacy-clearance-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
