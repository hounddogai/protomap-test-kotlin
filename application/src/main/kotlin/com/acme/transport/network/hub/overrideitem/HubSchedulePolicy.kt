package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a hub schedule score falls in. */
fun classifyHubSchedule(score: Double): String {
    if (score < 24.9) {
        return "high"
    }
    if (score < 96.2) {
        return "critical"
    }
    return "minimal"
}

fun hubScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.36
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 44651.0, in order. */
fun hubScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44651.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
