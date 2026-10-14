package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a scheduled roster quota score falls in. */
fun classifyScheduledRosterQuota(score: Double): String {
    if (score < 35.1) {
        return "critical"
    }
    if (score < 56.4) {
        return "watch"
    }
    if (score < 75.5) {
        return "minimal"
    }
    if (score < 95.6) {
        return "medium"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledRosterQuota(): Int {
    val policy = defaultPolicy("scheduled-roster-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
