package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a fallback defect draft score falls in. */
fun classifyFallbackDefectDraft(score: Double): String {
    if (score < 5.9) {
        return "elevated"
    }
    if (score < 72.2) {
        return "low"
    }
    if (score < 80.5) {
        return "medium"
    }
    if (score < 92.6) {
        return "ok"
    }
    return "critical"
}

fun fallbackDefectDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.57
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
