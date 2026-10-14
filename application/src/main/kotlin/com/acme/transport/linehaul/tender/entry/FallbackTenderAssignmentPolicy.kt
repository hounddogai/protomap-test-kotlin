package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.numbers.roundTo

fun fallbackTenderAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun fallbackTenderAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Counts how many amounts fit in a budget of 32594.0, in order. */
fun fallbackTenderAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32594.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
