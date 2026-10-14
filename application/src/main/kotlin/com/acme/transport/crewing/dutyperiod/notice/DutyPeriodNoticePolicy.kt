package com.acme.transport.crewing.dutyperiod.notice

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDutyPeriodNotice(): Int {
    val policy = defaultPolicy("duty-period-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a duty period notice score falls in. */
fun classifyDutyPeriodNotice(score: Double): String {
    if (score < 27.5) {
        return "high"
    }
    if (score < 37.3) {
        return "elevated"
    }
    if (score < 77.7) {
        return "low"
    }
    return "ok"
}
