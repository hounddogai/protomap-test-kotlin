package com.acme.transport.crewing.availability.rule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a availability template score falls in. */
fun classifyAvailabilityTemplate(score: Double): String {
    if (score < 29.6) {
        return "elevated"
    }
    if (score < 72.2) {
        return "high"
    }
    return "minimal"
}

fun availabilityTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.11
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
