package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun attemptEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAttemptEstimate(): Int {
    val policy = defaultPolicy("attempt-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a attempt estimate score falls in. */
fun classifyAttemptEstimate(score: Double): String {
    if (score < 25.8) {
        return "watch"
    }
    if (score < 35.4) {
        return "high"
    }
    if (score < 51.0) {
        return "minimal"
    }
    if (score < 64.6) {
        return "ok"
    }
    return "low"
}

fun attemptEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
