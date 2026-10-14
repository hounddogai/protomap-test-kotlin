package com.acme.transport.customs.broker.allocation

import com.acme.transport.kit.numbers.roundTo

fun brokerAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun brokerAllocationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 62697.0, in order. */
fun brokerAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62697.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
