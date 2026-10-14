package com.acme.transport.crewing.dutyperiod.bundle

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a duty period window score falls in. */
fun classifyDutyPeriodWindow(score: Double): String {
    if (score < 43.5) {
        return "medium"
    }
    if (score < 65.0) {
        return "minimal"
    }
    if (score < 67.2) {
        return "critical"
    }
    if (score < 93.5) {
        return "ok"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDutyPeriodWindow(): Int {
    val policy = defaultPolicy("duty-period-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
