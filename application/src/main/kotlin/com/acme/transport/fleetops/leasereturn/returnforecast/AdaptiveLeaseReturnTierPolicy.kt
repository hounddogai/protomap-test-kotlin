package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveLeaseReturnTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.09
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 37868.0, in order. */
fun adaptiveLeaseReturnTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37868.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveLeaseReturnTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
