package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a relay claim score falls in. */
fun classifyRelayClaim(score: Double): String {
    if (score < 6.8) {
        return "minimal"
    }
    if (score < 47.0) {
        return "elevated"
    }
    if (score < 88.3) {
        return "ok"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRelayClaim(): Int {
    val policy = defaultPolicy("relay-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
