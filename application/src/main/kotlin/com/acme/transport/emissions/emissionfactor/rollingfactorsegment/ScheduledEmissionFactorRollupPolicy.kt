package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledEmissionFactorRollup(): Int {
    val policy = defaultPolicy("scheduled-emission-factor-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 34016.0, in order. */
fun scheduledEmissionFactorRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34016.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledEmissionFactorRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
