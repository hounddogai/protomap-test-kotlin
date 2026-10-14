package com.acme.transport.network.cutoff.profile

/** Names the band a tiered cutoff variance score falls in. */
fun classifyTieredCutoffVariance(score: Double): String {
    if (score < 26.4) {
        return "high"
    }
    if (score < 41.5) {
        return "critical"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 22885.0, in order. */
fun tieredCutoffVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22885.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
