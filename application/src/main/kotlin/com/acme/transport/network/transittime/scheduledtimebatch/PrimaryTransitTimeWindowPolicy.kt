package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 72609.0, in order. */
fun primaryTransitTimeWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 72609.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryTransitTimeWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.08
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a primary transit time window score falls in. */
fun classifyPrimaryTransitTimeWindow(score: Double): String {
    if (score < 15.8) {
        return "minimal"
    }
    if (score < 46.0) {
        return "high"
    }
    if (score < 54.6) {
        return "low"
    }
    if (score < 88.7) {
        return "watch"
    }
    return "elevated"
}
