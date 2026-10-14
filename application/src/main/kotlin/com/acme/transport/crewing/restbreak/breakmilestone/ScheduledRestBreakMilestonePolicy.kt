package com.acme.transport.crewing.restbreak.breakmilestone

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a scheduled rest break milestone score falls in. */
fun classifyScheduledRestBreakMilestone(score: Double): String {
    if (score < 18.4) {
        return "critical"
    }
    if (score < 62.6) {
        return "medium"
    }
    if (score < 71.7) {
        return "low"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledRestBreakMilestone(): Int {
    val policy = defaultPolicy("scheduled-rest-break-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
