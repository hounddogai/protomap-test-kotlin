package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 64750.0, in order. */
fun scheduledFuelSurchargeBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64750.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledFuelSurchargeBundleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
