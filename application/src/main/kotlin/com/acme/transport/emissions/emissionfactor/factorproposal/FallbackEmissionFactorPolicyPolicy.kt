package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a fallback emission factor policy score falls in. */
fun classifyFallbackEmissionFactorPolicy(score: Double): String {
    if (score < 6.4) {
        return "minimal"
    }
    if (score < 34.1) {
        return "medium"
    }
    if (score < 79.4) {
        return "critical"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackEmissionFactorPolicy(): Int {
    val policy = defaultPolicy("fallback-emission-factor-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun fallbackEmissionFactorPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
