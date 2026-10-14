package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryBackhaulMetric(): Int {
    val policy = defaultPolicy("primary-backhaul-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 36576.0, in order. */
fun primaryBackhaulMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 36576.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a primary backhaul metric score falls in. */
fun classifyPrimaryBackhaulMetric(score: Double): String {
    if (score < 10.2) {
        return "ok"
    }
    if (score < 58.5) {
        return "low"
    }
    if (score < 72.5) {
        return "watch"
    }
    if (score < 78.6) {
        return "critical"
    }
    return "elevated"
}
