package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedDwellDraft(): Int {
    val policy = defaultPolicy("capped-dwell-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 89444.0, in order. */
fun cappedDwellDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 89444.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
