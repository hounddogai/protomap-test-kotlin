package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a manual zone summary score falls in. */
fun classifyManualZoneSummary(score: Double): String {
    if (score < 15.3) {
        return "critical"
    }
    if (score < 34.9) {
        return "ok"
    }
    if (score < 64.9) {
        return "elevated"
    }
    if (score < 85.6) {
        return "minimal"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 10465.0, in order. */
fun manualZoneSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10465.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun manualZoneSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
