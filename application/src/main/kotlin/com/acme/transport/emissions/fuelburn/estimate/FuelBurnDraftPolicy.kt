package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun fuelBurnDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelBurnDraft(): Int {
    val policy = defaultPolicy("fuel-burn-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
