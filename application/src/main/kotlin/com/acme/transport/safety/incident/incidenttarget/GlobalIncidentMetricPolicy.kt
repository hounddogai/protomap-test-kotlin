package com.acme.transport.safety.incident.incidenttarget

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalIncidentMetric(): Int {
    val policy = defaultPolicy("global-incident-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a global incident metric score falls in. */
fun classifyGlobalIncidentMetric(score: Double): String {
    if (score < 12.6) {
        return "watch"
    }
    if (score < 98.3) {
        return "medium"
    }
    return "minimal"
}

fun globalIncidentMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
