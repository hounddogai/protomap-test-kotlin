package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a defect rule score falls in. */
fun classifyDefectRule(score: Double): String {
    if (score < 23.6) {
        return "high"
    }
    if (score < 25.0) {
        return "watch"
    }
    if (score < 38.0) {
        return "low"
    }
    if (score < 64.2) {
        return "ok"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefectRule(): Int {
    val policy = defaultPolicy("defect-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
