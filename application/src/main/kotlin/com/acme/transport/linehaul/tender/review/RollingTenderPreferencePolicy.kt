package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a rolling tender preference score falls in. */
fun classifyRollingTenderPreference(score: Double): String {
    if (score < 7.1) {
        return "high"
    }
    if (score < 17.0) {
        return "critical"
    }
    if (score < 35.7) {
        return "ok"
    }
    if (score < 73.0) {
        return "medium"
    }
    return "elevated"
}

fun rollingTenderPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
