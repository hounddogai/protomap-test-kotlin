package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.numbers.roundTo

fun globalDwellBudgetBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 70895.0, in order. */
fun globalDwellBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70895.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
