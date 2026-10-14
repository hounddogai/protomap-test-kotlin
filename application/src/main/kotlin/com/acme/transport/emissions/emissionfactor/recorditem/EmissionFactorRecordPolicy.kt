package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun emissionFactorRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.1
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 11550.0, in order. */
fun emissionFactorRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11550.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
