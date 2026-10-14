package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 26265.0, in order. */
fun adaptiveScorecardLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 26265.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveScorecardLedger(): Int {
    val policy = defaultPolicy("adaptive-scorecard-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveScorecardLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
