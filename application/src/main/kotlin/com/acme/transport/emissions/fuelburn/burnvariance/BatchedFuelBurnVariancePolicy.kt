package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 28863.0, in order. */
fun batchedFuelBurnVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 28863.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun batchedFuelBurnVarianceBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a batched fuel burn variance score falls in. */
fun classifyBatchedFuelBurnVariance(score: Double): String {
    if (score < 4.7) {
        return "critical"
    }
    if (score < 31.2) {
        return "minimal"
    }
    return "high"
}
