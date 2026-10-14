package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTireNotice(): Int {
    val policy = defaultPolicy("tire-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a tire notice score falls in. */
fun classifyTireNotice(score: Double): String {
    if (score < 45.7) {
        return "medium"
    }
    if (score < 46.3) {
        return "low"
    }
    if (score < 63.5) {
        return "critical"
    }
    if (score < 84.6) {
        return "high"
    }
    return "elevated"
}
