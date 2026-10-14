package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a tiered ev charge plan score falls in. */
fun classifyTieredEvChargePlan(score: Double): String {
    if (score < 4.9) {
        return "high"
    }
    if (score < 8.3) {
        return "ok"
    }
    if (score < 23.5) {
        return "watch"
    }
    if (score < 78.7) {
        return "minimal"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredEvChargePlan(): Int {
    val policy = defaultPolicy("tiered-ev-charge-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun tieredEvChargePlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.58
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 30232.0, in order. */
fun tieredEvChargePlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30232.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
