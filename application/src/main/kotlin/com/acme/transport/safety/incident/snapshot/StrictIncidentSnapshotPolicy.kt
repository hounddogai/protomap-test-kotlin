package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a strict incident snapshot score falls in. */
fun classifyStrictIncidentSnapshot(score: Double): String {
    if (score < 17.6) {
        return "low"
    }
    if (score < 48.2) {
        return "high"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictIncidentSnapshot(): Int {
    val policy = defaultPolicy("strict-incident-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
