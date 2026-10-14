package com.acme.transport.crewing.availability.metric

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 78799.0, in order. */
fun strictAvailabilityMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78799.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictAvailabilityMetric(): Int {
    val policy = defaultPolicy("strict-availability-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a strict availability metric score falls in. */
fun classifyStrictAvailabilityMetric(score: Double): String {
    if (score < 25.1) {
        return "minimal"
    }
    if (score < 36.4) {
        return "medium"
    }
    if (score < 69.1) {
        return "low"
    }
    return "ok"
}
