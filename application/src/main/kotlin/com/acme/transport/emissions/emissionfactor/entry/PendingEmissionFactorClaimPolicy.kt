package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Counts how many amounts fit in a budget of 44762.0, in order. */
fun pendingEmissionFactorClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44762.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingEmissionFactorClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.23
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun pendingEmissionFactorClaimBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
