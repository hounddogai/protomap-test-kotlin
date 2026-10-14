package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 24201.0, in order. */
fun violationTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24201.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeViolationTier(): Int {
    val policy = defaultPolicy("violation-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
