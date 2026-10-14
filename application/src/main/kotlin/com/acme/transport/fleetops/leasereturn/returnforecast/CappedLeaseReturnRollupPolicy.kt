package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun cappedLeaseReturnRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.08
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 7185.0, in order. */
fun cappedLeaseReturnRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7185.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
