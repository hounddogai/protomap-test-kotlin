package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 86652.0, in order. */
fun tieredHubBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86652.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredHubBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.31
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
