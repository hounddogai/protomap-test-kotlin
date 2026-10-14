package com.acme.transport.safety.incident.manifest

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun incidentManifestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a incident manifest score falls in. */
fun classifyIncidentManifest(score: Double): String {
    if (score < 4.7) {
        return "elevated"
    }
    if (score < 10.1) {
        return "high"
    }
    if (score < 18.7) {
        return "medium"
    }
    return "low"
}

fun incidentManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeIncidentManifest(): Int {
    val policy = defaultPolicy("incident-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
