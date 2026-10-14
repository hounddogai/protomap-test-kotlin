package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a fallback defect record score falls in. */
fun classifyFallbackDefectRecord(score: Double): String {
    if (score < 37.2) {
        return "ok"
    }
    if (score < 53.6) {
        return "high"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackDefectRecord(): Int {
    val policy = defaultPolicy("fallback-defect-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
