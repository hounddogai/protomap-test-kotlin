package com.acme.transport.network.cutoff.forecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun defaultCutoffForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultCutoffForecast(): Int {
    val policy = defaultPolicy("default-cutoff-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 6790.0, in order. */
fun defaultCutoffForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6790.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a default cutoff forecast score falls in. */
fun classifyDefaultCutoffForecast(score: Double): String {
    if (score < 24.7) {
        return "watch"
    }
    if (score < 52.3) {
        return "elevated"
    }
    if (score < 75.8) {
        return "ok"
    }
    if (score < 77.8) {
        return "high"
    }
    return "critical"
}
