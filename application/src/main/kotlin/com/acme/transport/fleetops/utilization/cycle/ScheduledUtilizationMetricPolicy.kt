package com.acme.transport.fleetops.utilization.cycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a scheduled utilization metric score falls in. */
fun classifyScheduledUtilizationMetric(score: Double): String {
    if (score < 15.7) {
        return "medium"
    }
    if (score < 35.2) {
        return "minimal"
    }
    if (score < 50.9) {
        return "ok"
    }
    if (score < 60.2) {
        return "watch"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 35155.0, in order. */
fun scheduledUtilizationMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35155.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledUtilizationMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
