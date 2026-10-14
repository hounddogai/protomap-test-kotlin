package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a tiered incident cycle score falls in. */
fun classifyTieredIncidentCycle(score: Double): String {
    if (score < 41.3) {
        return "minimal"
    }
    if (score < 48.5) {
        return "critical"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredIncidentCycle(): Int {
    val policy = defaultPolicy("tiered-incident-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
