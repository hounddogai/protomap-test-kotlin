package com.acme.transport.crewing.roster.segment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a strict roster request score falls in. */
fun classifyStrictRosterRequest(score: Double): String {
    if (score < 9.6) {
        return "high"
    }
    if (score < 59.4) {
        return "elevated"
    }
    if (score < 70.1) {
        return "ok"
    }
    if (score < 82.4) {
        return "watch"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictRosterRequest(): Int {
    val policy = defaultPolicy("strict-roster-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
