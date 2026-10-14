package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.retry.defaultPolicy

fun adaptiveRouteOffsetManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a adaptive route offset manifest score falls in. */
fun classifyAdaptiveRouteOffsetManifest(score: Double): String {
    if (score < 17.4) {
        return "watch"
    }
    if (score < 19.4) {
        return "medium"
    }
    if (score < 83.3) {
        return "low"
    }
    if (score < 90.0) {
        return "critical"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveRouteOffsetManifest(): Int {
    val policy = defaultPolicy("adaptive-route-offset-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
