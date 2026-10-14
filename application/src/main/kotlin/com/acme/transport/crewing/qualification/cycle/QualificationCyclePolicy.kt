package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeQualificationCycle(): Int {
    val policy = defaultPolicy("qualification-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 10014.0, in order. */
fun qualificationCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10014.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
