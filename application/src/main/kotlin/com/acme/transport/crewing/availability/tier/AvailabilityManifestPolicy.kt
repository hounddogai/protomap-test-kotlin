package com.acme.transport.crewing.availability.tier

import com.acme.transport.kit.numbers.roundTo

fun availabilityManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 37006.0, in order. */
fun availabilityManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37006.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun availabilityManifestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
