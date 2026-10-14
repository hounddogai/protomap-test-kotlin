package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a idling draft score falls in. */
fun classifyIdlingDraft(score: Double): String {
    if (score < 59.7) {
        return "elevated"
    }
    if (score < 90.8) {
        return "low"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeIdlingDraft(): Int {
    val policy = defaultPolicy("idling-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
