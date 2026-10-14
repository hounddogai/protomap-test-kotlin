package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.numbers.roundTo

fun nearMissLedgerBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 90237.0, in order. */
fun nearMissLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90237.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a near miss ledger score falls in. */
fun classifyNearMissLedger(score: Double): String {
    if (score < 22.5) {
        return "ok"
    }
    if (score < 95.1) {
        return "elevated"
    }
    return "medium"
}
