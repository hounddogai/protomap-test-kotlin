package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerYardSchedule(): Int {
    val policy = defaultPolicy("trailer-yard-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a trailer yard schedule score falls in. */
fun classifyTrailerYardSchedule(score: Double): String {
    if (score < 2.6) {
        return "critical"
    }
    if (score < 13.5) {
        return "medium"
    }
    if (score < 14.2) {
        return "elevated"
    }
    if (score < 41.7) {
        return "low"
    }
    return "minimal"
}
