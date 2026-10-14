package com.acme.transport.customs.clearance.schedule

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a scheduled clearance notice score falls in. */
fun classifyScheduledClearanceNotice(score: Double): String {
    if (score < 10.1) {
        return "high"
    }
    if (score < 30.5) {
        return "critical"
    }
    if (score < 90.9) {
        return "medium"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledClearanceNotice(): Int {
    val policy = defaultPolicy("scheduled-clearance-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
