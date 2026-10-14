package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun weightedTireCycleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 66809.0, in order. */
fun weightedTireCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66809.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a weighted tire cycle score falls in. */
fun classifyWeightedTireCycle(score: Double): String {
    if (score < 18.2) {
        return "ok"
    }
    if (score < 69.8) {
        return "low"
    }
    if (score < 79.7) {
        return "high"
    }
    if (score < 91.4) {
        return "minimal"
    }
    return "medium"
}

fun weightedTireCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
