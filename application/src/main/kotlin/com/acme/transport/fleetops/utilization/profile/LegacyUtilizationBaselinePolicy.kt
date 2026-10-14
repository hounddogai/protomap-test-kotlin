package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 11801.0, in order. */
fun legacyUtilizationBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11801.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun legacyUtilizationBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.06
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
