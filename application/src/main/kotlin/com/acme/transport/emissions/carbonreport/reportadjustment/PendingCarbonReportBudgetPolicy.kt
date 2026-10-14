package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun pendingCarbonReportBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a pending carbon report budget score falls in. */
fun classifyPendingCarbonReportBudget(score: Double): String {
    if (score < 74.5) {
        return "ok"
    }
    if (score < 75.2) {
        return "minimal"
    }
    if (score < 81.1) {
        return "watch"
    }
    if (score < 92.0) {
        return "low"
    }
    return "high"
}

fun pendingCarbonReportBudgetBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 52090.0, in order. */
fun pendingCarbonReportBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 52090.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
