package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalEmissionFactorTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.03
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 43647.0, in order. */
fun regionalEmissionFactorTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43647.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a regional emission factor target score falls in. */
fun classifyRegionalEmissionFactorTarget(score: Double): String {
    if (score < 10.1) {
        return "watch"
    }
    if (score < 19.0) {
        return "ok"
    }
    if (score < 55.4) {
        return "minimal"
    }
    if (score < 73.9) {
        return "critical"
    }
    return "elevated"
}
