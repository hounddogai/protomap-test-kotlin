package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun cappedAvailabilityPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a capped availability preference score falls in. */
fun classifyCappedAvailabilityPreference(score: Double): String {
    if (score < 84.9) {
        return "watch"
    }
    if (score < 96.8) {
        return "high"
    }
    return "low"
}
