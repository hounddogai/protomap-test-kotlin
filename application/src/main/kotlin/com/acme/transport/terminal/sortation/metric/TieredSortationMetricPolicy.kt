package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredSortationMetric(): Int {
    val policy = defaultPolicy("tiered-sortation-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun tieredSortationMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.06
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a tiered sortation metric score falls in. */
fun classifyTieredSortationMetric(score: Double): String {
    if (score < 44.0) {
        return "critical"
    }
    if (score < 97.7) {
        return "medium"
    }
    if (score < 99.3) {
        return "ok"
    }
    if (score < 99.9) {
        return "high"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 27270.0, in order. */
fun tieredSortationMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 27270.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
