package com.acme.transport.fleetops.inspection.summary

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeInspectionSummary(): Int {
    val policy = defaultPolicy("inspection-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun inspectionSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a inspection summary score falls in. */
fun classifyInspectionSummary(score: Double): String {
    if (score < 54.1) {
        return "minimal"
    }
    if (score < 72.5) {
        return "high"
    }
    return "medium"
}

fun inspectionSummaryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
