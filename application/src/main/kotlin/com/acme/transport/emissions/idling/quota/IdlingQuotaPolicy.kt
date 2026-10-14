package com.acme.transport.emissions.idling.quota

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeIdlingQuota(): Int {
    val policy = defaultPolicy("idling-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a idling quota score falls in. */
fun classifyIdlingQuota(score: Double): String {
    if (score < 3.3) {
        return "low"
    }
    if (score < 35.3) {
        return "critical"
    }
    if (score < 52.1) {
        return "high"
    }
    if (score < 85.0) {
        return "medium"
    }
    return "watch"
}
