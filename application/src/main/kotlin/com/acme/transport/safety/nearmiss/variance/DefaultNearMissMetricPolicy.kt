package com.acme.transport.safety.nearmiss.variance

/** Counts how many amounts fit in a budget of 70688.0, in order. */
fun defaultNearMissMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70688.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a default near miss metric score falls in. */
fun classifyDefaultNearMissMetric(score: Double): String {
    if (score < 7.6) {
        return "ok"
    }
    if (score < 22.6) {
        return "critical"
    }
    if (score < 60.5) {
        return "elevated"
    }
    if (score < 74.2) {
        return "high"
    }
    return "minimal"
}
