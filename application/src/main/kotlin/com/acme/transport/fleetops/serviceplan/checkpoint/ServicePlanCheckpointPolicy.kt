package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a service plan checkpoint score falls in. */
fun classifyServicePlanCheckpoint(score: Double): String {
    if (score < 6.9) {
        return "medium"
    }
    if (score < 14.0) {
        return "watch"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanCheckpoint(): Int {
    val policy = defaultPolicy("service-plan-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun servicePlanCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun servicePlanCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
