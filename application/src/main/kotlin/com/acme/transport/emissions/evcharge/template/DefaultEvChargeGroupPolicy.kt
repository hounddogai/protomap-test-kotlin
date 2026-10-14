package com.acme.transport.emissions.evcharge.template

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 66745.0, in order. */
fun defaultEvChargeGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66745.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun defaultEvChargeGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
