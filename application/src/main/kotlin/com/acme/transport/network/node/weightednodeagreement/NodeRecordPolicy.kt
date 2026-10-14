package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.numbers.roundTo

fun nodeRecordBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 18733.0, in order. */
fun nodeRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 18733.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
