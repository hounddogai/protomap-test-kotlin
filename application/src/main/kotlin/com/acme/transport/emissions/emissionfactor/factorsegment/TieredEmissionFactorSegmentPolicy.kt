package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tieredEmissionFactorSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 85015.0, in order. */
fun tieredEmissionFactorSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85015.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a tiered emission factor segment score falls in. */
fun classifyTieredEmissionFactorSegment(score: Double): String {
    if (score < 22.2) {
        return "low"
    }
    if (score < 99.8) {
        return "high"
    }
    return "medium"
}
