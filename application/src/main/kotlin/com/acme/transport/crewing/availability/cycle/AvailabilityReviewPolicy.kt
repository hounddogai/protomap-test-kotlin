package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun availabilityReviewBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun availabilityReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a availability review score falls in. */
fun classifyAvailabilityReview(score: Double): String {
    if (score < 8.9) {
        return "high"
    }
    if (score < 99.4) {
        return "low"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAvailabilityReview(): Int {
    val policy = defaultPolicy("availability-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
