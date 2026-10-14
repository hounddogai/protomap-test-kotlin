package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultCarbonReportAssignment(): Int {
    val policy = defaultPolicy("default-carbon-report-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun defaultCarbonReportAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a default carbon report assignment score falls in. */
fun classifyDefaultCarbonReportAssignment(score: Double): String {
    if (score < 19.2) {
        return "low"
    }
    if (score < 29.1) {
        return "medium"
    }
    if (score < 30.9) {
        return "minimal"
    }
    if (score < 71.6) {
        return "watch"
    }
    return "critical"
}

fun defaultCarbonReportAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
