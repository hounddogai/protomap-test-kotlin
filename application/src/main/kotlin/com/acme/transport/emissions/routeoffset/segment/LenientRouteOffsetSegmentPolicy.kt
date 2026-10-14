package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun lenientRouteOffsetSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun lenientRouteOffsetSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.21
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a lenient route offset segment score falls in. */
fun classifyLenientRouteOffsetSegment(score: Double): String {
    if (score < 24.3) {
        return "low"
    }
    if (score < 71.8) {
        return "minimal"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientRouteOffsetSegment(): Int {
    val policy = defaultPolicy("lenient-route-offset-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
