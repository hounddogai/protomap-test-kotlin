package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.numbers.roundTo

fun fuelBurnAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a fuel burn adjustment score falls in. */
fun classifyFuelBurnAdjustment(score: Double): String {
    if (score < 38.6) {
        return "minimal"
    }
    if (score < 88.2) {
        return "critical"
    }
    return "medium"
}

fun fuelBurnAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 5847.0, in order. */
fun fuelBurnAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5847.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
