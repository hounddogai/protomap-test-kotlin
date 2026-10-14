package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a adaptive inspection manifest score falls in. */
fun classifyAdaptiveInspectionManifest(score: Double): String {
    if (score < 20.3) {
        return "watch"
    }
    if (score < 37.3) {
        return "medium"
    }
    if (score < 82.3) {
        return "low"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveInspectionManifest(): Int {
    val policy = defaultPolicy("adaptive-inspection-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
