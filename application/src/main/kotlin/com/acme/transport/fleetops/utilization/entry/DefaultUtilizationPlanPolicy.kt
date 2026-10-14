package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.kit.retry.defaultPolicy

fun defaultUtilizationPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultUtilizationPlan(): Int {
    val policy = defaultPolicy("default-utilization-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
