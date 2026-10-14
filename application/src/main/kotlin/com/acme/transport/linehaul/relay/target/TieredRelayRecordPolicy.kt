package com.acme.transport.linehaul.relay.target

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a tiered relay record score falls in. */
fun classifyTieredRelayRecord(score: Double): String {
    if (score < 36.7) {
        return "ok"
    }
    if (score < 55.6) {
        return "critical"
    }
    if (score < 71.5) {
        return "low"
    }
    if (score < 71.5) {
        return "minimal"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredRelayRecord(): Int {
    val policy = defaultPolicy("tiered-relay-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun tieredRelayRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
