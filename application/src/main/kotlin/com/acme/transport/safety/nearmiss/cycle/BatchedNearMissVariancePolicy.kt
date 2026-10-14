package com.acme.transport.safety.nearmiss.cycle

import com.acme.transport.kit.numbers.roundTo

/** Names the band a batched near miss variance score falls in. */
fun classifyBatchedNearMissVariance(score: Double): String {
    if (score < 20.0) {
        return "critical"
    }
    if (score < 35.4) {
        return "elevated"
    }
    if (score < 62.8) {
        return "medium"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 25964.0, in order. */
fun batchedNearMissVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25964.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun batchedNearMissVarianceBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
