package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a violation adjustment score falls in. */
fun classifyViolationAdjustment(score: Double): String {
    if (score < 26.5) {
        return "minimal"
    }
    if (score < 33.4) {
        return "high"
    }
    if (score < 75.2) {
        return "medium"
    }
    return "watch"
}

fun violationAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun violationAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeViolationAdjustment(): Int {
    val policy = defaultPolicy("violation-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
