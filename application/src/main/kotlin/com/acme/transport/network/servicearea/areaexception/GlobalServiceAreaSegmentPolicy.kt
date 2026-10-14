package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a global service area segment score falls in. */
fun classifyGlobalServiceAreaSegment(score: Double): String {
    if (score < 24.2) {
        return "critical"
    }
    if (score < 65.6) {
        return "ok"
    }
    if (score < 96.9) {
        return "high"
    }
    return "low"
}

fun globalServiceAreaSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun globalServiceAreaSegmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
