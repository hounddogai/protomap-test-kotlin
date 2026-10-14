package com.acme.transport.customs.inspection.digest

import com.acme.transport.kit.numbers.roundTo

fun inspectionSlotBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 94064.0, in order. */
fun inspectionSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94064.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun inspectionSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
