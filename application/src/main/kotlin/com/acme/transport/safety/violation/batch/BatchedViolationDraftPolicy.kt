package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedViolationDraft(): Int {
    val policy = defaultPolicy("batched-violation-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun batchedViolationDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a batched violation draft score falls in. */
fun classifyBatchedViolationDraft(score: Double): String {
    if (score < 64.3) {
        return "low"
    }
    if (score < 84.9) {
        return "medium"
    }
    if (score < 96.1) {
        return "watch"
    }
    if (score < 99.3) {
        return "elevated"
    }
    return "ok"
}

fun batchedViolationDraftBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
