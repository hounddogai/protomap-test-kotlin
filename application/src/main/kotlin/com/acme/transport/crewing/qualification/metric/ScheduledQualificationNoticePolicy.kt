package com.acme.transport.crewing.qualification.metric

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledQualificationNotice(): Int {
    val policy = defaultPolicy("scheduled-qualification-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a scheduled qualification notice score falls in. */
fun classifyScheduledQualificationNotice(score: Double): String {
    if (score < 43.2) {
        return "ok"
    }
    if (score < 45.6) {
        return "high"
    }
    if (score < 76.3) {
        return "low"
    }
    return "critical"
}
