package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun regionalIncidentExceptionBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun regionalIncidentExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a regional incident exception score falls in. */
fun classifyRegionalIncidentException(score: Double): String {
    if (score < 4.9) {
        return "minimal"
    }
    if (score < 37.6) {
        return "high"
    }
    if (score < 54.8) {
        return "watch"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalIncidentException(): Int {
    val policy = defaultPolicy("regional-incident-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
