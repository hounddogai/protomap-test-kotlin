package com.acme.transport.emissions.idling.idlingquota

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 57505.0, in order. */
fun manualIdlingTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 57505.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun manualIdlingTargetBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
