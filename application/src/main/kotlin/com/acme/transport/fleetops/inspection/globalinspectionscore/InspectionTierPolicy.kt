package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun inspectionTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a inspection tier score falls in. */
fun classifyInspectionTier(score: Double): String {
    if (score < 1.9) {
        return "medium"
    }
    if (score < 16.9) {
        return "critical"
    }
    return "ok"
}

fun inspectionTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
