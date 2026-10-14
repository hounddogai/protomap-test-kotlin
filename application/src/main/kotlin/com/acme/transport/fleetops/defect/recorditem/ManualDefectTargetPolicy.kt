package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun manualDefectTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun manualDefectTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a manual defect target score falls in. */
fun classifyManualDefectTarget(score: Double): String {
    if (score < 11.7) {
        return "critical"
    }
    if (score < 67.3) {
        return "watch"
    }
    if (score < 80.0) {
        return "minimal"
    }
    if (score < 92.5) {
        return "low"
    }
    return "ok"
}
