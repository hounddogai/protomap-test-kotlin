package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun weightedViolationCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedViolationCycle(): Int {
    val policy = defaultPolicy("weighted-violation-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a weighted violation cycle score falls in. */
fun classifyWeightedViolationCycle(score: Double): String {
    if (score < 27.0) {
        return "minimal"
    }
    if (score < 51.5) {
        return "elevated"
    }
    if (score < 67.2) {
        return "high"
    }
    if (score < 80.6) {
        return "critical"
    }
    return "watch"
}

fun weightedViolationCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
