package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRouteOffsetVariance(): Int {
    val policy = defaultPolicy("route-offset-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a route offset variance score falls in. */
fun classifyRouteOffsetVariance(score: Double): String {
    if (score < 11.7) {
        return "critical"
    }
    if (score < 16.0) {
        return "low"
    }
    if (score < 37.2) {
        return "high"
    }
    if (score < 45.8) {
        return "minimal"
    }
    return "medium"
}

fun routeOffsetVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun routeOffsetVarianceBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
