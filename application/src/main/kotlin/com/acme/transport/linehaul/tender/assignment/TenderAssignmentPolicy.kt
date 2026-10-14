package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a tender assignment score falls in. */
fun classifyTenderAssignment(score: Double): String {
    if (score < 39.7) {
        return "low"
    }
    if (score < 64.3) {
        return "minimal"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTenderAssignment(): Int {
    val policy = defaultPolicy("tender-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun tenderAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
