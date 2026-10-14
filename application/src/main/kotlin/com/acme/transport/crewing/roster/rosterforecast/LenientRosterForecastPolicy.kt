package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a lenient roster forecast score falls in. */
fun classifyLenientRosterForecast(score: Double): String {
    if (score < 63.8) {
        return "medium"
    }
    if (score < 86.2) {
        return "low"
    }
    return "ok"
}

fun lenientRosterForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Counts how many amounts fit in a budget of 90731.0, in order. */
fun lenientRosterForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90731.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientRosterForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
