package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tieredEvChargeScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.19
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 20900.0, in order. */
fun tieredEvChargeScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20900.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
