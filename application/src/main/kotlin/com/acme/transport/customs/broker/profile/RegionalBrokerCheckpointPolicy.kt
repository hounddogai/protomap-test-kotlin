package com.acme.transport.customs.broker.profile

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalBrokerCheckpoint(): Int {
    val policy = defaultPolicy("regional-broker-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a regional broker checkpoint score falls in. */
fun classifyRegionalBrokerCheckpoint(score: Double): String {
    if (score < 28.9) {
        return "elevated"
    }
    if (score < 44.1) {
        return "ok"
    }
    if (score < 67.7) {
        return "low"
    }
    if (score < 96.0) {
        return "minimal"
    }
    return "high"
}
