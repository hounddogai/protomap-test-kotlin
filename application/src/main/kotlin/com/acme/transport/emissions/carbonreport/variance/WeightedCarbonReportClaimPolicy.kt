package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 78512.0, in order. */
fun weightedCarbonReportClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78512.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a weighted carbon report claim score falls in. */
fun classifyWeightedCarbonReportClaim(score: Double): String {
    if (score < 53.0) {
        return "high"
    }
    if (score < 97.4) {
        return "medium"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedCarbonReportClaim(): Int {
    val policy = defaultPolicy("weighted-carbon-report-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
