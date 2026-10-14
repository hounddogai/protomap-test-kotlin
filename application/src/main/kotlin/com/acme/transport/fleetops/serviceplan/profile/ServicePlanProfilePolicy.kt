package com.acme.transport.fleetops.serviceplan.profile

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanProfile(): Int {
    val policy = defaultPolicy("service-plan-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 2403.0, in order. */
fun servicePlanProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2403.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun servicePlanProfileBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun servicePlanProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.59
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
