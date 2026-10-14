package com.acme.transport.customs.inspection.draft

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a inspection metric score falls in. */
fun classifyInspectionMetric(score: Double): String {
    if (score < 16.7) {
        return "medium"
    }
    if (score < 18.8) {
        return "ok"
    }
    if (score < 24.1) {
        return "high"
    }
    return "minimal"
}

fun inspectionMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun inspectionMetricBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun inspectionMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.53
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
