package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 41493.0, in order. */
fun seasonalAvailabilityManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41493.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun seasonalAvailabilityManifestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
