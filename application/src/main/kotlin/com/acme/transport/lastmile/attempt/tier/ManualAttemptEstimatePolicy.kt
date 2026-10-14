package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.numbers.roundTo

fun manualAttemptEstimateBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 69455.0, in order. */
fun manualAttemptEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 69455.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
