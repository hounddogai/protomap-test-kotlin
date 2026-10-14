package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 84315.0, in order. */
fun weightedFuelBurnSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 84315.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun weightedFuelBurnSummaryBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a weighted fuel burn summary score falls in. */
fun classifyWeightedFuelBurnSummary(score: Double): String {
    if (score < 64.2) {
        return "minimal"
    }
    if (score < 85.0) {
        return "watch"
    }
    return "ok"
}
