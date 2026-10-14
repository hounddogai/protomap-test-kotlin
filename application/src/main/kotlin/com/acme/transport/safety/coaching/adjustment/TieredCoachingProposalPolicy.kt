package com.acme.transport.safety.coaching.adjustment

/** Counts how many amounts fit in a budget of 48885.0, in order. */
fun tieredCoachingProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48885.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredCoachingProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
