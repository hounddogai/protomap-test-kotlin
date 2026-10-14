package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun scheduledEmissionFactorMetricBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledEmissionFactorMetric(): Int {
    val policy = defaultPolicy("scheduled-emission-factor-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scheduledEmissionFactorMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
