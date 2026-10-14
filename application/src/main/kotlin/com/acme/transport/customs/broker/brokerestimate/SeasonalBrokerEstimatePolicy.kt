package com.acme.transport.customs.broker.brokerestimate

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 74216.0, in order. */
fun seasonalBrokerEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74216.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalBrokerEstimate(): Int {
    val policy = defaultPolicy("seasonal-broker-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a seasonal broker estimate score falls in. */
fun classifySeasonalBrokerEstimate(score: Double): String {
    if (score < 24.7) {
        return "medium"
    }
    if (score < 50.2) {
        return "critical"
    }
    if (score < 85.4) {
        return "watch"
    }
    if (score < 86.1) {
        return "ok"
    }
    return "elevated"
}

fun seasonalBrokerEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
