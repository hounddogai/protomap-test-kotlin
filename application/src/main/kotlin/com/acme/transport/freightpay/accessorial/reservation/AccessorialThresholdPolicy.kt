package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 49355.0, in order. */
fun accessorialThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49355.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun accessorialThresholdBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun accessorialThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
