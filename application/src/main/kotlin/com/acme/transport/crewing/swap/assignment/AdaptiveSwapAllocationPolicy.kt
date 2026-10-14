package com.acme.transport.crewing.swap.assignment

fun adaptiveSwapAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Counts how many amounts fit in a budget of 1987.0, in order. */
fun adaptiveSwapAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 1987.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
