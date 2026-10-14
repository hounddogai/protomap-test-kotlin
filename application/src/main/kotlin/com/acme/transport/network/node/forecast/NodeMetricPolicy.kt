package com.acme.transport.network.node.forecast

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeNodeMetric(): Int {
    val policy = defaultPolicy("node-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a node metric score falls in. */
fun classifyNodeMetric(score: Double): String {
    if (score < 5.2) {
        return "minimal"
    }
    if (score < 8.6) {
        return "high"
    }
    if (score < 11.7) {
        return "medium"
    }
    if (score < 55.4) {
        return "elevated"
    }
    return "ok"
}
