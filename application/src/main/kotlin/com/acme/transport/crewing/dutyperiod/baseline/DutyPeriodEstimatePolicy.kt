package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a duty period estimate score falls in. */
fun classifyDutyPeriodEstimate(score: Double): String {
    if (score < 4.7) {
        return "ok"
    }
    if (score < 24.1) {
        return "high"
    }
    if (score < 65.9) {
        return "critical"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDutyPeriodEstimate(): Int {
    val policy = defaultPolicy("duty-period-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
