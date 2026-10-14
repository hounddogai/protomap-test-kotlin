package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun weightedEmissionFactorGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.41
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun weightedEmissionFactorGroupBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 67912.0, in order. */
fun weightedEmissionFactorGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 67912.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a weighted emission factor group score falls in. */
fun classifyWeightedEmissionFactorGroup(score: Double): String {
    if (score < 15.0) {
        return "ok"
    }
    if (score < 79.5) {
        return "watch"
    }
    if (score < 94.8) {
        return "low"
    }
    return "minimal"
}
