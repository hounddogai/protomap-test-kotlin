package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalFuelBurnRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 66866.0, in order. */
fun regionalFuelBurnRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66866.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
