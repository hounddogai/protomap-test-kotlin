package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.numbers.roundTo

fun tieredAttemptMetricBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 89497.0, in order. */
fun tieredAttemptMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 89497.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
