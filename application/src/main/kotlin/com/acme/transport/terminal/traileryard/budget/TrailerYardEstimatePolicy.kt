package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerYardEstimate(): Int {
    val policy = defaultPolicy("trailer-yard-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun trailerYardEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
