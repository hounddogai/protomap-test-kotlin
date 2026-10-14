package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictDisputeSummary(): Int {
    val policy = defaultPolicy("strict-dispute-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 69425.0, in order. */
fun strictDisputeSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 69425.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
