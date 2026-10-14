package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a hold exception score falls in. */
fun classifyHoldException(score: Double): String {
    if (score < 46.2) {
        return "watch"
    }
    if (score < 81.2) {
        return "minimal"
    }
    if (score < 87.3) {
        return "critical"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHoldException(): Int {
    val policy = defaultPolicy("hold-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
