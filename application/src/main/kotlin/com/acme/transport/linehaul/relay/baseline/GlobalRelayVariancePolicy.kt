package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalRelayVariance(): Int {
    val policy = defaultPolicy("global-relay-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun globalRelayVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a global relay variance score falls in. */
fun classifyGlobalRelayVariance(score: Double): String {
    if (score < 4.5) {
        return "medium"
    }
    if (score < 9.3) {
        return "ok"
    }
    if (score < 58.8) {
        return "watch"
    }
    if (score < 68.7) {
        return "high"
    }
    return "low"
}
