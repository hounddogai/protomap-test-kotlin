package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a fallback route offset profile score falls in. */
fun classifyFallbackRouteOffsetProfile(score: Double): String {
    if (score < 50.9) {
        return "ok"
    }
    if (score < 77.8) {
        return "high"
    }
    if (score < 83.7) {
        return "minimal"
    }
    if (score < 99.9) {
        return "elevated"
    }
    return "critical"
}

fun fallbackRouteOffsetProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun fallbackRouteOffsetProfileBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackRouteOffsetProfile(): Int {
    val policy = defaultPolicy("fallback-route-offset-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
