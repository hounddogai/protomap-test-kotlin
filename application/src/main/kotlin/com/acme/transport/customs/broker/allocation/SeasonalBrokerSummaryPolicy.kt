package com.acme.transport.customs.broker.allocation

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalBrokerSummary(): Int {
    val policy = defaultPolicy("seasonal-broker-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 89581.0, in order. */
fun seasonalBrokerSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 89581.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a seasonal broker summary score falls in. */
fun classifySeasonalBrokerSummary(score: Double): String {
    if (score < 12.8) {
        return "elevated"
    }
    if (score < 18.3) {
        return "minimal"
    }
    if (score < 79.8) {
        return "high"
    }
    if (score < 79.8) {
        return "low"
    }
    return "ok"
}
