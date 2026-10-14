package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalRedeliveryLedger(): Int {
    val policy = defaultPolicy("seasonal-redelivery-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 46686.0, in order. */
fun seasonalRedeliveryLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46686.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
