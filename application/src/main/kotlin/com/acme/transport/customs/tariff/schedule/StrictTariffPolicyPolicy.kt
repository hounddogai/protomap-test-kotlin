package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun strictTariffPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a strict tariff policy score falls in. */
fun classifyStrictTariffPolicy(score: Double): String {
    if (score < 9.1) {
        return "low"
    }
    if (score < 17.9) {
        return "elevated"
    }
    return "high"
}
