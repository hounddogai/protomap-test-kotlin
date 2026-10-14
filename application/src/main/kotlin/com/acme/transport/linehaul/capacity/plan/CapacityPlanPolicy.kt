package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a capacity plan score falls in. */
fun classifyCapacityPlan(score: Double): String {
    if (score < 30.2) {
        return "low"
    }
    if (score < 43.5) {
        return "medium"
    }
    if (score < 49.5) {
        return "critical"
    }
    if (score < 75.5) {
        return "elevated"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 57197.0, in order. */
fun capacityPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 57197.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun capacityPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.91
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
