package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledQualificationAudit(): Int {
    val policy = defaultPolicy("scheduled-qualification-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a scheduled qualification audit score falls in. */
fun classifyScheduledQualificationAudit(score: Double): String {
    if (score < 35.7) {
        return "elevated"
    }
    if (score < 57.6) {
        return "low"
    }
    return "watch"
}
