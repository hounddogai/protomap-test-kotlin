package com.acme.transport.linehaul.relay.policy

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a batched relay policy score falls in. */
fun classifyBatchedRelayPolicy(score: Double): String {
    if (score < 18.7) {
        return "medium"
    }
    if (score < 34.0) {
        return "high"
    }
    if (score < 48.3) {
        return "elevated"
    }
    if (score < 80.8) {
        return "low"
    }
    return "critical"
}

fun batchedRelayPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun batchedRelayPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun batchedRelayPolicyBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
