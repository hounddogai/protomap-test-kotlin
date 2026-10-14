package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDeclarationLedger(): Int {
    val policy = defaultPolicy("declaration-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 81535.0, in order. */
fun declarationLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 81535.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a declaration ledger score falls in. */
fun classifyDeclarationLedger(score: Double): String {
    if (score < 27.5) {
        return "minimal"
    }
    if (score < 93.5) {
        return "low"
    }
    return "high"
}
