package com.acme.transport.crewing.roster.digest

import com.acme.transport.kit.retry.defaultPolicy

fun batchedRosterReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedRosterReview(): Int {
    val policy = defaultPolicy("batched-roster-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a batched roster review score falls in. */
fun classifyBatchedRosterReview(score: Double): String {
    if (score < 4.6) {
        return "elevated"
    }
    if (score < 16.0) {
        return "ok"
    }
    if (score < 53.5) {
        return "minimal"
    }
    if (score < 69.5) {
        return "low"
    }
    return "medium"
}
