package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a lenient backhaul variance score falls in. */
fun classifyLenientBackhaulVariance(score: Double): String {
    if (score < 19.6) {
        return "high"
    }
    if (score < 84.5) {
        return "critical"
    }
    if (score < 88.9) {
        return "ok"
    }
    return "low"
}

fun lenientBackhaulVarianceBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun lenientBackhaulVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun lenientBackhaulVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
