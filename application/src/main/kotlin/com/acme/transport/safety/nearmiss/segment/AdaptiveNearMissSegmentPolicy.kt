package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun adaptiveNearMissSegmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun adaptiveNearMissSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveNearMissSegment(): Int {
    val policy = defaultPolicy("adaptive-near-miss-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a adaptive near miss segment score falls in. */
fun classifyAdaptiveNearMissSegment(score: Double): String {
    if (score < 67.6) {
        return "low"
    }
    if (score < 87.1) {
        return "medium"
    }
    if (score < 90.6) {
        return "elevated"
    }
    return "ok"
}
