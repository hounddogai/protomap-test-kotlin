package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServiceAreaEntry(): Int {
    val policy = defaultPolicy("service-area-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a service area entry score falls in. */
fun classifyServiceAreaEntry(score: Double): String {
    if (score < 40.7) {
        return "minimal"
    }
    if (score < 46.2) {
        return "high"
    }
    return "medium"
}
