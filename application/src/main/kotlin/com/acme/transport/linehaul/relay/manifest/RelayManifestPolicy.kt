package com.acme.transport.linehaul.relay.manifest

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRelayManifest(): Int {
    val policy = defaultPolicy("relay-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a relay manifest score falls in. */
fun classifyRelayManifest(score: Double): String {
    if (score < 49.8) {
        return "high"
    }
    if (score < 91.8) {
        return "minimal"
    }
    return "low"
}
