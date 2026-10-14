package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun coachingBaselineBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a coaching baseline score falls in. */
fun classifyCoachingBaseline(score: Double): String {
    if (score < 15.8) {
        return "critical"
    }
    if (score < 82.6) {
        return "minimal"
    }
    return "watch"
}

fun coachingBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCoachingBaseline(): Int {
    val policy = defaultPolicy("coaching-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
