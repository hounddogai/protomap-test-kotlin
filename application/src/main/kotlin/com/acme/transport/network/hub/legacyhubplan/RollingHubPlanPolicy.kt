package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun rollingHubPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a rolling hub plan score falls in. */
fun classifyRollingHubPlan(score: Double): String {
    if (score < 3.1) {
        return "ok"
    }
    if (score < 9.2) {
        return "elevated"
    }
    if (score < 74.7) {
        return "critical"
    }
    if (score < 99.8) {
        return "watch"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingHubPlan(): Int {
    val policy = defaultPolicy("rolling-hub-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rollingHubPlanBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
