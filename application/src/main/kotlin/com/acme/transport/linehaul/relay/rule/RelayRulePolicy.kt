package com.acme.transport.linehaul.relay.rule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun relayRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRelayRule(): Int {
    val policy = defaultPolicy("relay-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a relay rule score falls in. */
fun classifyRelayRule(score: Double): String {
    if (score < 20.4) {
        return "low"
    }
    if (score < 62.6) {
        return "ok"
    }
    if (score < 86.9) {
        return "medium"
    }
    if (score < 98.4) {
        return "critical"
    }
    return "watch"
}
