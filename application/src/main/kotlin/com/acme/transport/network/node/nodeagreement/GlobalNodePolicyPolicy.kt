package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a global node policy score falls in. */
fun classifyGlobalNodePolicy(score: Double): String {
    if (score < 24.8) {
        return "low"
    }
    if (score < 47.8) {
        return "high"
    }
    if (score < 60.0) {
        return "elevated"
    }
    if (score < 82.2) {
        return "watch"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalNodePolicy(): Int {
    val policy = defaultPolicy("global-node-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun globalNodePolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.23
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
