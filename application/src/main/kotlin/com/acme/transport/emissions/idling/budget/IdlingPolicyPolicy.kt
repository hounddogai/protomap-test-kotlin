package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun idlingPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.95
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeIdlingPolicy(): Int {
    val policy = defaultPolicy("idling-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
