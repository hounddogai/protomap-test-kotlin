package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.kit.numbers.roundTo

fun lanePlanProposalBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 16059.0, in order. */
fun lanePlanProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 16059.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lanePlanProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
