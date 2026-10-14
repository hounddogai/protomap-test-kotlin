package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.kit.retry.defaultPolicy

fun incidentBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a incident budget score falls in. */
fun classifyIncidentBudget(score: Double): String {
    if (score < 24.1) {
        return "low"
    }
    if (score < 85.2) {
        return "critical"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeIncidentBudget(): Int {
    val policy = defaultPolicy("incident-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
