package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun fallbackServicePlanRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun fallbackServicePlanRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.87
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fallback service plan rollup score falls in. */
fun classifyFallbackServicePlanRollup(score: Double): String {
    if (score < 33.7) {
        return "low"
    }
    if (score < 59.2) {
        return "elevated"
    }
    if (score < 64.2) {
        return "minimal"
    }
    if (score < 87.2) {
        return "watch"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackServicePlanRollup(): Int {
    val policy = defaultPolicy("fallback-service-plan-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
