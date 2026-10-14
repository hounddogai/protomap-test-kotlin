package com.acme.transport.customs.clearance.clearancecheckpoint

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun clearanceProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 28712.0, in order. */
fun clearanceProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 28712.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
