package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeNodeProfile(): Int {
    val policy = defaultPolicy("node-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a node profile score falls in. */
fun classifyNodeProfile(score: Double): String {
    if (score < 19.1) {
        return "critical"
    }
    if (score < 41.7) {
        return "high"
    }
    if (score < 58.8) {
        return "watch"
    }
    return "ok"
}
