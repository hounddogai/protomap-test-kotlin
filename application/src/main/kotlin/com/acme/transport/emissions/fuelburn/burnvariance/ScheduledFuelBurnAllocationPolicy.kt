package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a scheduled fuel burn allocation score falls in. */
fun classifyScheduledFuelBurnAllocation(score: Double): String {
    if (score < 21.9) {
        return "watch"
    }
    if (score < 94.7) {
        return "medium"
    }
    return "low"
}

fun scheduledFuelBurnAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.23
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun scheduledFuelBurnAllocationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledFuelBurnAllocation(): Int {
    val policy = defaultPolicy("scheduled-fuel-burn-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
