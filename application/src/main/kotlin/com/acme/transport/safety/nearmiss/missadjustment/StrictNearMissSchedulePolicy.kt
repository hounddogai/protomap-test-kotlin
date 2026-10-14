package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictNearMissSchedule(): Int {
    val policy = defaultPolicy("strict-near-miss-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 70225.0, in order. */
fun strictNearMissScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70225.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
