package com.acme.transport.fleetops.utilization.target

fun utilizationSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Counts how many amounts fit in a budget of 90478.0, in order. */
fun utilizationSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90478.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
