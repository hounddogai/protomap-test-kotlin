package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedAvailabilityGroup(): Int {
    val policy = defaultPolicy("weighted-availability-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun weightedAvailabilityGroupBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun weightedAvailabilityGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
