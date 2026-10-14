package com.acme.transport.freightpay.accessorial.accessorialbudget

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a adaptive accessorial slot score falls in. */
fun classifyAdaptiveAccessorialSlot(score: Double): String {
    if (score < 76.1) {
        return "minimal"
    }
    if (score < 79.9) {
        return "elevated"
    }
    if (score < 88.2) {
        return "critical"
    }
    if (score < 93.6) {
        return "medium"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveAccessorialSlot(): Int {
    val policy = defaultPolicy("adaptive-accessorial-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
