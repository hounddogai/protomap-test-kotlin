package com.acme.transport.emissions.idling.allocation

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 77420.0, in order. */
fun lenientIdlingAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77420.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientIdlingAllocationBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
