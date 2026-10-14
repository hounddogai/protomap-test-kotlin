package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a seasonal route offset threshold score falls in. */
fun classifySeasonalRouteOffsetThreshold(score: Double): String {
    if (score < 18.0) {
        return "critical"
    }
    if (score < 99.1) {
        return "ok"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 18115.0, in order. */
fun seasonalRouteOffsetThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 18115.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalRouteOffsetThreshold(): Int {
    val policy = defaultPolicy("seasonal-route-offset-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
