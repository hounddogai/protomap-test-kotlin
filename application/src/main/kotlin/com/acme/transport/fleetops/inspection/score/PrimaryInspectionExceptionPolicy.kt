package com.acme.transport.fleetops.inspection.score

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a primary inspection exception score falls in. */
fun classifyPrimaryInspectionException(score: Double): String {
    if (score < 12.3) {
        return "low"
    }
    if (score < 21.9) {
        return "ok"
    }
    if (score < 55.1) {
        return "elevated"
    }
    if (score < 69.5) {
        return "minimal"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryInspectionException(): Int {
    val policy = defaultPolicy("primary-inspection-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun primaryInspectionExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
