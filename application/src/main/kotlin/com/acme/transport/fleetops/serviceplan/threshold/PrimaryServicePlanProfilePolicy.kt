package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryServicePlanProfile(): Int {
    val policy = defaultPolicy("primary-service-plan-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun primaryServicePlanProfileBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a primary service plan profile score falls in. */
fun classifyPrimaryServicePlanProfile(score: Double): String {
    if (score < 25.5) {
        return "critical"
    }
    if (score < 88.2) {
        return "ok"
    }
    if (score < 90.8) {
        return "watch"
    }
    return "minimal"
}
