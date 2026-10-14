package com.acme.transport.lastmile.attempt.plan

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a lenient attempt plan score falls in. */
fun classifyLenientAttemptPlan(score: Double): String {
    if (score < 50.0) {
        return "minimal"
    }
    if (score < 86.4) {
        return "watch"
    }
    return "ok"
}

fun lenientAttemptPlanBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientAttemptPlan(): Int {
    val policy = defaultPolicy("lenient-attempt-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
