package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultParcelMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a default parcel metric score falls in. */
fun classifyDefaultParcelMetric(score: Double): String {
    if (score < 8.8) {
        return "medium"
    }
    if (score < 42.5) {
        return "high"
    }
    if (score < 44.0) {
        return "critical"
    }
    if (score < 75.5) {
        return "low"
    }
    return "minimal"
}
