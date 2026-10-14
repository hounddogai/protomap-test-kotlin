package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun seasonalClearanceMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a seasonal clearance milestone score falls in. */
fun classifySeasonalClearanceMilestone(score: Double): String {
    if (score < 34.2) {
        return "high"
    }
    if (score < 62.6) {
        return "minimal"
    }
    if (score < 75.0) {
        return "low"
    }
    return "critical"
}

fun seasonalClearanceMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun seasonalClearanceMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
