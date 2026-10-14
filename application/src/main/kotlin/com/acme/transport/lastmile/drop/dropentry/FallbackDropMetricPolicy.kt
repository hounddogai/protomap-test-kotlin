package com.acme.transport.lastmile.drop.dropentry

import com.acme.transport.kit.numbers.roundTo

fun fallbackDropMetricBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a fallback drop metric score falls in. */
fun classifyFallbackDropMetric(score: Double): String {
    if (score < 10.8) {
        return "minimal"
    }
    if (score < 24.9) {
        return "medium"
    }
    if (score < 75.1) {
        return "low"
    }
    if (score < 83.0) {
        return "high"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 40534.0, in order. */
fun fallbackDropMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40534.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
