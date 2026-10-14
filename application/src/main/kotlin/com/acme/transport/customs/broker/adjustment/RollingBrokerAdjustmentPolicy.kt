package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.numbers.roundTo

fun rollingBrokerAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 26461.0, in order. */
fun rollingBrokerAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 26461.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rollingBrokerAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
