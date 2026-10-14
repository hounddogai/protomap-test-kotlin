package com.acme.transport.linehaul.trailer.trailerbatch

import com.acme.transport.kit.numbers.roundTo

fun batchedTrailerBatchBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 95961.0, in order. */
fun batchedTrailerBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 95961.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
