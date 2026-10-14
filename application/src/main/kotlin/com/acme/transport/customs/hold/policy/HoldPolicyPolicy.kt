package com.acme.transport.customs.hold.policy

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHoldPolicy(): Int {
    val policy = defaultPolicy("hold-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a hold policy score falls in. */
fun classifyHoldPolicy(score: Double): String {
    if (score < 49.9) {
        return "high"
    }
    if (score < 57.7) {
        return "elevated"
    }
    return "watch"
}

fun holdPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
