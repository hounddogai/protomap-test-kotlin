package com.acme.transport.network.zone.zonetier

import com.acme.transport.kit.numbers.roundTo

fun primaryZoneTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun primaryZoneTierBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 69048.0, in order. */
fun primaryZoneTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 69048.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
