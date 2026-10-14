package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun globalViolationBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun globalViolationBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a global violation batch score falls in. */
fun classifyGlobalViolationBatch(score: Double): String {
    if (score < 60.0) {
        return "high"
    }
    if (score < 86.5) {
        return "minimal"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalViolationBatch(): Int {
    val policy = defaultPolicy("global-violation-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
