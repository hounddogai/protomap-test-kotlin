package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.kit.retry.defaultPolicy

fun servicePlanQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanQuota(): Int {
    val policy = defaultPolicy("service-plan-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
