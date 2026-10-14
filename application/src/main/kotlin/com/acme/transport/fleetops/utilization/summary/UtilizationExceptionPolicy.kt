package com.acme.transport.fleetops.utilization.summary

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 2649.0, in order. */
fun utilizationExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2649.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeUtilizationException(): Int {
    val policy = defaultPolicy("utilization-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a utilization exception score falls in. */
fun classifyUtilizationException(score: Double): String {
    if (score < 8.9) {
        return "ok"
    }
    if (score < 9.1) {
        return "medium"
    }
    if (score < 25.4) {
        return "low"
    }
    return "critical"
}

fun utilizationExceptionBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
