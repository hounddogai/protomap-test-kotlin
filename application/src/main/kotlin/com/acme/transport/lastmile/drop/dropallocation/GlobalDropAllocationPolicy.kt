package com.acme.transport.lastmile.drop.dropallocation

/** Counts how many amounts fit in a budget of 98451.0, in order. */
fun globalDropAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98451.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalDropAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
