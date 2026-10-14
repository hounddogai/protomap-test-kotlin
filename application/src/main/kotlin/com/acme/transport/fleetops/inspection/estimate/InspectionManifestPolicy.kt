package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a inspection manifest score falls in. */
fun classifyInspectionManifest(score: Double): String {
    if (score < 37.8) {
        return "high"
    }
    if (score < 70.1) {
        return "critical"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeInspectionManifest(): Int {
    val policy = defaultPolicy("inspection-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
