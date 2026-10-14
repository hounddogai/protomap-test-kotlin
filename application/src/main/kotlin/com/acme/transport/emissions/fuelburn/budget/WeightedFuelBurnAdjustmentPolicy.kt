package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun weightedFuelBurnAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.82
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun weightedFuelBurnAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a weighted fuel burn adjustment score falls in. */
fun classifyWeightedFuelBurnAdjustment(score: Double): String {
    if (score < 10.2) {
        return "low"
    }
    if (score < 49.7) {
        return "watch"
    }
    if (score < 67.1) {
        return "elevated"
    }
    if (score < 80.4) {
        return "medium"
    }
    return "critical"
}
