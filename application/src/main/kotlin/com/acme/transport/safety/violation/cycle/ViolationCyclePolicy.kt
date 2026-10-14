package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a violation cycle score falls in. */
fun classifyViolationCycle(score: Double): String {
    if (score < 57.0) {
        return "low"
    }
    if (score < 79.9) {
        return "high"
    }
    if (score < 80.3) {
        return "medium"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeViolationCycle(): Int {
    val policy = defaultPolicy("violation-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
