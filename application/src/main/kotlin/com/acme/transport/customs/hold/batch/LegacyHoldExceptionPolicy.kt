package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.numbers.roundTo

fun legacyHoldExceptionBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 94299.0, in order. */
fun legacyHoldExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94299.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
