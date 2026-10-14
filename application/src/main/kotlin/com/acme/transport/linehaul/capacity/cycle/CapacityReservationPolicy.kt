package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun capacityReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a capacity reservation score falls in. */
fun classifyCapacityReservation(score: Double): String {
    if (score < 7.3) {
        return "minimal"
    }
    if (score < 35.7) {
        return "watch"
    }
    if (score < 94.8) {
        return "critical"
    }
    if (score < 96.8) {
        return "low"
    }
    return "high"
}
