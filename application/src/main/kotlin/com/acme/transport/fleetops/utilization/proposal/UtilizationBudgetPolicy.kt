package com.acme.transport.fleetops.utilization.proposal

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 39144.0, in order. */
fun utilizationBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 39144.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun utilizationBudgetBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
