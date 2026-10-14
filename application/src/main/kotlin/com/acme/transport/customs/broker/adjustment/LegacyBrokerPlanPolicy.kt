package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyBrokerPlan(): Int {
    val policy = defaultPolicy("legacy-broker-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun legacyBrokerPlanBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun legacyBrokerPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a legacy broker plan score falls in. */
fun classifyLegacyBrokerPlan(score: Double): String {
    if (score < 69.6) {
        return "elevated"
    }
    if (score < 94.9) {
        return "high"
    }
    return "ok"
}
