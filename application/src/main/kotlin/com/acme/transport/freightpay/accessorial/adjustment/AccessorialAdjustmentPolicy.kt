package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAccessorialAdjustment(): Int {
    val policy = defaultPolicy("accessorial-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun accessorialAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a accessorial adjustment score falls in. */
fun classifyAccessorialAdjustment(score: Double): String {
    if (score < 13.8) {
        return "low"
    }
    if (score < 59.9) {
        return "critical"
    }
    return "elevated"
}
