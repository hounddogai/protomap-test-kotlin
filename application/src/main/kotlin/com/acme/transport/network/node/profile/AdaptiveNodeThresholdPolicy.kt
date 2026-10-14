package com.acme.transport.network.node.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a adaptive node threshold score falls in. */
fun classifyAdaptiveNodeThreshold(score: Double): String {
    if (score < 39.9) {
        return "watch"
    }
    if (score < 63.0) {
        return "high"
    }
    return "medium"
}

fun adaptiveNodeThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.93
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 99242.0, in order. */
fun adaptiveNodeThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99242.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
