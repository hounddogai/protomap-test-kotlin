package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun manualViolationWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.42
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a manual violation window score falls in. */
fun classifyManualViolationWindow(score: Double): String {
    if (score < 35.5) {
        return "high"
    }
    if (score < 52.5) {
        return "critical"
    }
    if (score < 87.2) {
        return "low"
    }
    return "medium"
}
