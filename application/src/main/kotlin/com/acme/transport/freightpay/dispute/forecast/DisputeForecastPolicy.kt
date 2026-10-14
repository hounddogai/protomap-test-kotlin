package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 34573.0, in order. */
fun disputeForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34573.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDisputeForecast(): Int {
    val policy = defaultPolicy("dispute-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a dispute forecast score falls in. */
fun classifyDisputeForecast(score: Double): String {
    if (score < 25.5) {
        return "medium"
    }
    if (score < 64.9) {
        return "elevated"
    }
    return "critical"
}
