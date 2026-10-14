package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedViolationTier(): Int {
    val policy = defaultPolicy("batched-violation-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a batched violation tier score falls in. */
fun classifyBatchedViolationTier(score: Double): String {
    if (score < 31.7) {
        return "medium"
    }
    if (score < 52.7) {
        return "low"
    }
    if (score < 55.6) {
        return "ok"
    }
    if (score < 84.5) {
        return "watch"
    }
    return "high"
}

fun batchedViolationTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun batchedViolationTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
