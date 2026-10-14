package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefectPlan(): Int {
    val policy = defaultPolicy("defect-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a defect plan score falls in. */
fun classifyDefectPlan(score: Double): String {
    if (score < 11.2) {
        return "minimal"
    }
    if (score < 23.7) {
        return "elevated"
    }
    return "watch"
}
