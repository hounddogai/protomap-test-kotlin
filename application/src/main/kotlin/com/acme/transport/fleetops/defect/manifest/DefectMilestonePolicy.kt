package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defectMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.82
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 11829.0, in order. */
fun defectMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11829.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
