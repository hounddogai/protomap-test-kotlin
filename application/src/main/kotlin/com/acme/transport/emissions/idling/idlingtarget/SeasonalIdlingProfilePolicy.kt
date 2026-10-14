package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun seasonalIdlingProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun seasonalIdlingProfileBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a seasonal idling profile score falls in. */
fun classifySeasonalIdlingProfile(score: Double): String {
    if (score < 12.7) {
        return "low"
    }
    if (score < 70.2) {
        return "critical"
    }
    if (score < 84.2) {
        return "elevated"
    }
    return "medium"
}
