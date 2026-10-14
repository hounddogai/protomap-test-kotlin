package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAccessorialBaseline(): Int {
    val policy = defaultPolicy("accessorial-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a accessorial baseline score falls in. */
fun classifyAccessorialBaseline(score: Double): String {
    if (score < 77.9) {
        return "critical"
    }
    if (score < 88.5) {
        return "watch"
    }
    return "elevated"
}
