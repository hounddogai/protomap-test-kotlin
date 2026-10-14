package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredFuelBurnSlot(): Int {
    val policy = defaultPolicy("tiered-fuel-burn-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun tieredFuelBurnSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.7
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
