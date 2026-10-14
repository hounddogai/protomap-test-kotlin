package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun seasonalEmissionFactorOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a seasonal emission factor override score falls in. */
fun classifySeasonalEmissionFactorOverride(score: Double): String {
    if (score < 14.4) {
        return "high"
    }
    if (score < 36.6) {
        return "elevated"
    }
    if (score < 65.3) {
        return "medium"
    }
    if (score < 92.0) {
        return "minimal"
    }
    return "ok"
}
