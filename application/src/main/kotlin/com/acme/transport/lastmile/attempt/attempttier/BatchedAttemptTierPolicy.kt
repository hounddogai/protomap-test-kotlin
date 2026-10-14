package com.acme.transport.lastmile.attempt.attempttier

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a batched attempt tier score falls in. */
fun classifyBatchedAttemptTier(score: Double): String {
    if (score < 17.2) {
        return "minimal"
    }
    if (score < 24.6) {
        return "critical"
    }
    if (score < 48.6) {
        return "low"
    }
    return "elevated"
}

fun batchedAttemptTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedAttemptTier(): Int {
    val policy = defaultPolicy("batched-attempt-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
