package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalViolationProposal(): Int {
    val policy = defaultPolicy("global-violation-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a global violation proposal score falls in. */
fun classifyGlobalViolationProposal(score: Double): String {
    if (score < 11.3) {
        return "low"
    }
    if (score < 19.4) {
        return "ok"
    }
    if (score < 35.7) {
        return "watch"
    }
    if (score < 52.0) {
        return "critical"
    }
    return "medium"
}
