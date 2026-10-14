package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun cutoffBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.41
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCutoffBudget(): Int {
    val policy = defaultPolicy("cutoff-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a cutoff budget score falls in. */
fun classifyCutoffBudget(score: Double): String {
    if (score < 9.4) {
        return "minimal"
    }
    if (score < 11.6) {
        return "low"
    }
    if (score < 38.3) {
        return "critical"
    }
    if (score < 97.2) {
        return "high"
    }
    return "ok"
}
