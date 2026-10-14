package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 90990.0, in order. */
fun lenientAvailabilityTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90990.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientAvailabilityTier(): Int {
    val policy = defaultPolicy("lenient-availability-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
