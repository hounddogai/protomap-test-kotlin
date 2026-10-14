package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 19486.0, in order. */
fun clearanceEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19486.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a clearance estimate score falls in. */
fun classifyClearanceEstimate(score: Double): String {
    if (score < 44.4) {
        return "watch"
    }
    if (score < 50.9) {
        return "critical"
    }
    if (score < 71.9) {
        return "elevated"
    }
    if (score < 81.0) {
        return "minimal"
    }
    return "medium"
}

fun clearanceEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.03
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
