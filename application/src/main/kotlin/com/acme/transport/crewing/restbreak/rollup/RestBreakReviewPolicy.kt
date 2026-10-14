package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun restBreakReviewBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a rest break review score falls in. */
fun classifyRestBreakReview(score: Double): String {
    if (score < 53.4) {
        return "medium"
    }
    if (score < 68.4) {
        return "minimal"
    }
    if (score < 73.2) {
        return "high"
    }
    if (score < 88.1) {
        return "elevated"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRestBreakReview(): Int {
    val policy = defaultPolicy("rest-break-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
