package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun pendingFuelBurnWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.46
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a pending fuel burn window score falls in. */
fun classifyPendingFuelBurnWindow(score: Double): String {
    if (score < 14.2) {
        return "low"
    }
    if (score < 72.0) {
        return "minimal"
    }
    return "high"
}
