package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingAttemptEntry(): Int {
    val policy = defaultPolicy("pending-attempt-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a pending attempt entry score falls in. */
fun classifyPendingAttemptEntry(score: Double): String {
    if (score < 19.3) {
        return "minimal"
    }
    if (score < 43.6) {
        return "high"
    }
    if (score < 90.4) {
        return "low"
    }
    if (score < 91.3) {
        return "elevated"
    }
    return "medium"
}
