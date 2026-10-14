package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun seasonalDwellProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a seasonal dwell profile score falls in. */
fun classifySeasonalDwellProfile(score: Double): String {
    if (score < 57.2) {
        return "critical"
    }
    if (score < 99.5) {
        return "high"
    }
    return "elevated"
}
