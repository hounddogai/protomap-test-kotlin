package com.acme.transport.safety.violation.violationslot

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalViolationSlot(): Int {
    val policy = defaultPolicy("regional-violation-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a regional violation slot score falls in. */
fun classifyRegionalViolationSlot(score: Double): String {
    if (score < 26.7) {
        return "watch"
    }
    if (score < 43.5) {
        return "critical"
    }
    if (score < 66.7) {
        return "elevated"
    }
    if (score < 91.9) {
        return "minimal"
    }
    return "medium"
}
