package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun inspectionVarianceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a inspection variance score falls in. */
fun classifyInspectionVariance(score: Double): String {
    if (score < 13.6) {
        return "medium"
    }
    if (score < 19.9) {
        return "ok"
    }
    if (score < 22.5) {
        return "low"
    }
    if (score < 42.0) {
        return "minimal"
    }
    return "critical"
}

fun inspectionVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun inspectionVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
