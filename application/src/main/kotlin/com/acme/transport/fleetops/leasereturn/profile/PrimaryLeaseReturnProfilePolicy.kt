package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 59019.0, in order. */
fun primaryLeaseReturnProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 59019.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryLeaseReturnProfileBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
