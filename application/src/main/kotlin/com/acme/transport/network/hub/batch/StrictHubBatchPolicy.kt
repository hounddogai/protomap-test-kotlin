package com.acme.transport.network.hub.batch

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictHubBatch(): Int {
    val policy = defaultPolicy("strict-hub-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a strict hub batch score falls in. */
fun classifyStrictHubBatch(score: Double): String {
    if (score < 17.4) {
        return "minimal"
    }
    if (score < 32.7) {
        return "medium"
    }
    if (score < 39.1) {
        return "low"
    }
    if (score < 89.2) {
        return "watch"
    }
    return "critical"
}
