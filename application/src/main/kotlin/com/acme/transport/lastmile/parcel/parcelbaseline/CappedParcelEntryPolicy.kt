package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 1609.0, in order. */
fun cappedParcelEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 1609.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedParcelEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun cappedParcelEntryBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
