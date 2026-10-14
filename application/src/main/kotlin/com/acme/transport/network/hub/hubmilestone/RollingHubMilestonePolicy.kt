package com.acme.transport.network.hub.hubmilestone

import com.acme.transport.kit.numbers.roundTo

fun rollingHubMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 2074.0, in order. */
fun rollingHubMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2074.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
