package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDutyPeriodReview(): Int {
    val policy = defaultPolicy("duty-period-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a duty period review score falls in. */
fun classifyDutyPeriodReview(score: Double): String {
    if (score < 29.7) {
        return "watch"
    }
    if (score < 54.3) {
        return "high"
    }
    if (score < 65.8) {
        return "low"
    }
    if (score < 89.3) {
        return "critical"
    }
    return "ok"
}
