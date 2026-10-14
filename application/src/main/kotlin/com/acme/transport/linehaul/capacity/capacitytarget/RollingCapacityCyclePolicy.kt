package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingCapacityCycle(): Int {
    val policy = defaultPolicy("rolling-capacity-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 24032.0, in order. */
fun rollingCapacityCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24032.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
