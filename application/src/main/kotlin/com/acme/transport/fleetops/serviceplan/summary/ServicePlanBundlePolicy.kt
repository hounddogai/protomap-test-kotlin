package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun servicePlanBundleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanBundle(): Int {
    val policy = defaultPolicy("service-plan-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
