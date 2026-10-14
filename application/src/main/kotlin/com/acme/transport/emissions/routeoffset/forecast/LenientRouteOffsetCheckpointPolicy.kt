package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun lenientRouteOffsetCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Counts how many amounts fit in a budget of 44764.0, in order. */
fun lenientRouteOffsetCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44764.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientRouteOffsetCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientRouteOffsetCheckpoint(): Int {
    val policy = defaultPolicy("lenient-route-offset-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
