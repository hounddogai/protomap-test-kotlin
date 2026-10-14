package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingClearanceAllocation(): Int {
    val policy = defaultPolicy("pending-clearance-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun pendingClearanceAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a pending clearance allocation score falls in. */
fun classifyPendingClearanceAllocation(score: Double): String {
    if (score < 59.5) {
        return "elevated"
    }
    if (score < 67.4) {
        return "critical"
    }
    if (score < 78.8) {
        return "ok"
    }
    return "low"
}
