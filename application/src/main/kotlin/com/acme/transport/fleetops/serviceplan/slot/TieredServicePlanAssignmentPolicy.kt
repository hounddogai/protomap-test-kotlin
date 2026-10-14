package com.acme.transport.fleetops.serviceplan.slot

import com.acme.transport.kit.numbers.roundTo

fun tieredServicePlanAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 44816.0, in order. */
fun tieredServicePlanAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44816.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredServicePlanAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
