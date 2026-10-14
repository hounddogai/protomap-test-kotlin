package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 76286.0, in order. */
fun sortationLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76286.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSortationLedger(): Int {
    val policy = defaultPolicy("sortation-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
