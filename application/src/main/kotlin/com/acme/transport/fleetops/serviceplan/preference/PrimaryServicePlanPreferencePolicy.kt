package com.acme.transport.fleetops.serviceplan.preference

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun primaryServicePlanPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryServicePlanPreference(): Int {
    val policy = defaultPolicy("primary-service-plan-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
