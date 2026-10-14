package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun zoneBaselineBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a zone baseline score falls in. */
fun classifyZoneBaseline(score: Double): String {
    if (score < 16.0) {
        return "elevated"
    }
    if (score < 27.1) {
        return "critical"
    }
    if (score < 76.7) {
        return "medium"
    }
    if (score < 77.1) {
        return "high"
    }
    return "ok"
}

fun zoneBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
