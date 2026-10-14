package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredEmissionFactorSchedule(): Int {
    val policy = defaultPolicy("tiered-emission-factor-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun tieredEmissionFactorScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 20606.0, in order. */
fun tieredEmissionFactorScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20606.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredEmissionFactorScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
