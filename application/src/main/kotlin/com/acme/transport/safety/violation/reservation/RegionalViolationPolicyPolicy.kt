package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a regional violation policy score falls in. */
fun classifyRegionalViolationPolicy(score: Double): String {
    if (score < 25.5) {
        return "medium"
    }
    if (score < 34.3) {
        return "elevated"
    }
    if (score < 52.3) {
        return "critical"
    }
    if (score < 80.7) {
        return "ok"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalViolationPolicy(): Int {
    val policy = defaultPolicy("regional-violation-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
