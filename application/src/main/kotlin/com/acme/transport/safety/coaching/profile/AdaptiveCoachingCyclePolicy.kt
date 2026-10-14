package com.acme.transport.safety.coaching.profile

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveCoachingCycle(): Int {
    val policy = defaultPolicy("adaptive-coaching-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveCoachingCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Names the band a adaptive coaching cycle score falls in. */
fun classifyAdaptiveCoachingCycle(score: Double): String {
    if (score < 45.2) {
        return "high"
    }
    if (score < 50.0) {
        return "medium"
    }
    if (score < 76.5) {
        return "low"
    }
    if (score < 86.0) {
        return "ok"
    }
    return "elevated"
}
