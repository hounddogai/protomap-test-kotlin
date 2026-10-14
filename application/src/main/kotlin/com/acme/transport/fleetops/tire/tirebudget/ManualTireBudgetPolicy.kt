package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualTireBudget(): Int {
    val policy = defaultPolicy("manual-tire-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun manualTireBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
