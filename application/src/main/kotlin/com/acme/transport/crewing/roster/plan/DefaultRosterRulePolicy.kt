package com.acme.transport.crewing.roster.plan

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 10668.0, in order. */
fun defaultRosterRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10668.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultRosterRule(): Int {
    val policy = defaultPolicy("default-roster-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
