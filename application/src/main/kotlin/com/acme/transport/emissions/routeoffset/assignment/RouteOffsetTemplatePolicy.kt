package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a route offset template score falls in. */
fun classifyRouteOffsetTemplate(score: Double): String {
    if (score < 33.9) {
        return "ok"
    }
    if (score < 57.4) {
        return "minimal"
    }
    if (score < 70.6) {
        return "low"
    }
    if (score < 77.4) {
        return "medium"
    }
    return "watch"
}

fun routeOffsetTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRouteOffsetTemplate(): Int {
    val policy = defaultPolicy("route-offset-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
