package com.acme.transport.crewing.swap.audit

import com.acme.transport.kit.numbers.roundTo

fun swapAuditBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 65773.0, in order. */
fun swapAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 65773.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a swap audit score falls in. */
fun classifySwapAudit(score: Double): String {
    if (score < 13.9) {
        return "minimal"
    }
    if (score < 44.2) {
        return "medium"
    }
    return "low"
}
