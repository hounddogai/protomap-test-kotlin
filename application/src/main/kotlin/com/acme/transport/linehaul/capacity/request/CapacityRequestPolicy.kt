package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a capacity request score falls in. */
fun classifyCapacityRequest(score: Double): String {
    if (score < 66.8) {
        return "medium"
    }
    if (score < 76.7) {
        return "ok"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCapacityRequest(): Int {
    val policy = defaultPolicy("capacity-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
