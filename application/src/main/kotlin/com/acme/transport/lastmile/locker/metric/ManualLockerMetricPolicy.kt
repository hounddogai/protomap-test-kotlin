package com.acme.transport.lastmile.locker.metric

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun manualLockerMetricBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 77434.0, in order. */
fun manualLockerMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77434.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun manualLockerMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualLockerMetric(): Int {
    val policy = defaultPolicy("manual-locker-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
