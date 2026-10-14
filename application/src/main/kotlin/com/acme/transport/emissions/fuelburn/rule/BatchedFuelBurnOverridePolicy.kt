package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun batchedFuelBurnOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.25
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun batchedFuelBurnOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 71327.0, in order. */
fun batchedFuelBurnOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71327.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
