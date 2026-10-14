package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.numbers.roundTo

fun strictSortationBatchBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 53658.0, in order. */
fun strictSortationBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53658.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
