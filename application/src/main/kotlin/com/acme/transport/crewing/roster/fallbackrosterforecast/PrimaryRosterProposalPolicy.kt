package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.kit.numbers.roundTo

fun primaryRosterProposalBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 82611.0, in order. */
fun primaryRosterProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82611.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
