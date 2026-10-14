package com.acme.transport.customs.broker.audit

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun manualBrokerOverrideBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualBrokerOverride(): Int {
    val policy = defaultPolicy("manual-broker-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun manualBrokerOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a manual broker override score falls in. */
fun classifyManualBrokerOverride(score: Double): String {
    if (score < 35.9) {
        return "elevated"
    }
    if (score < 44.7) {
        return "watch"
    }
    if (score < 57.3) {
        return "medium"
    }
    return "low"
}
