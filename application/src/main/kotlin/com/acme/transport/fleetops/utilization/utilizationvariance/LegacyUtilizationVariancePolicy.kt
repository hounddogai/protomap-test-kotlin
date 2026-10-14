package com.acme.transport.fleetops.utilization.utilizationvariance

/** Counts how many amounts fit in a budget of 61306.0, in order. */
fun legacyUtilizationVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61306.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a legacy utilization variance score falls in. */
fun classifyLegacyUtilizationVariance(score: Double): String {
    if (score < 30.8) {
        return "elevated"
    }
    if (score < 33.9) {
        return "watch"
    }
    if (score < 74.5) {
        return "minimal"
    }
    if (score < 92.8) {
        return "ok"
    }
    return "high"
}

fun legacyUtilizationVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
