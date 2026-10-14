package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.numbers.roundTo

/** Names the band a fuel burn forecast score falls in. */
fun classifyFuelBurnForecast(score: Double): String {
    if (score < 52.6) {
        return "elevated"
    }
    if (score < 78.6) {
        return "minimal"
    }
    if (score < 92.8) {
        return "watch"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 46552.0, in order. */
fun fuelBurnForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46552.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fuelBurnForecastBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun fuelBurnForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
