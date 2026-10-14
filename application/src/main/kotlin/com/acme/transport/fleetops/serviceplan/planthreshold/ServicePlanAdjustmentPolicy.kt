package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun servicePlanAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a service plan adjustment score falls in. */
fun classifyServicePlanAdjustment(score: Double): String {
    if (score < 25.9) {
        return "minimal"
    }
    if (score < 65.5) {
        return "watch"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanAdjustment(): Int {
    val policy = defaultPolicy("service-plan-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
