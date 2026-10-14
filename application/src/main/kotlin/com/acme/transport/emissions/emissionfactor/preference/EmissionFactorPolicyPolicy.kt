package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a emission factor policy score falls in. */
fun classifyEmissionFactorPolicy(score: Double): String {
    if (score < 2.9) {
        return "medium"
    }
    if (score < 32.5) {
        return "ok"
    }
    if (score < 35.3) {
        return "watch"
    }
    return "minimal"
}

fun emissionFactorPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
