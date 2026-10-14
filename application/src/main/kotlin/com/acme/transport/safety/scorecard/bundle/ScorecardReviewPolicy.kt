package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun scorecardReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScorecardReview(): Int {
    val policy = defaultPolicy("scorecard-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scorecardReviewBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a scorecard review score falls in. */
fun classifyScorecardReview(score: Double): String {
    if (score < 6.8) {
        return "minimal"
    }
    if (score < 81.6) {
        return "ok"
    }
    return "watch"
}
