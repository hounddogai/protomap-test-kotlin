package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.numbers.roundTo

fun rosterCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 34026.0, in order. */
fun rosterCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34026.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
