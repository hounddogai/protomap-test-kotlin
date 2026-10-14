package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 54282.0, in order. */
fun legacyTirePlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54282.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun legacyTirePlanBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
