package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a drop schedule score falls in. */
fun classifyDropSchedule(score: Double): String {
    if (score < 18.7) {
        return "critical"
    }
    if (score < 59.6) {
        return "high"
    }
    if (score < 78.9) {
        return "watch"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDropSchedule(): Int {
    val policy = defaultPolicy("drop-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
