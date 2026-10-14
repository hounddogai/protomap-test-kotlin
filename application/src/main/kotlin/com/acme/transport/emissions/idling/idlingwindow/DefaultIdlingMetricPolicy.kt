package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultIdlingMetric(): Int {
    val policy = defaultPolicy("default-idling-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 77882.0, in order. */
fun defaultIdlingMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77882.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun defaultIdlingMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
