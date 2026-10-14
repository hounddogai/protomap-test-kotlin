package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 62968.0, in order. */
fun scheduledRateCardRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62968.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledRateCardRollup(): Int {
    val policy = defaultPolicy("scheduled-rate-card-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
