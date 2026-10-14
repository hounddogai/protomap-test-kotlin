package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 40361.0, in order. */
fun defaultSwapOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40361.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultSwapOverride(): Int {
    val policy = defaultPolicy("default-swap-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun defaultSwapOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun defaultSwapOverrideBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
