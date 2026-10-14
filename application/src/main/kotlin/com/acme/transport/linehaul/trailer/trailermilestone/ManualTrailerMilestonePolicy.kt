package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun manualTrailerMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.2
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun manualTrailerMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Names the band a manual trailer milestone score falls in. */
fun classifyManualTrailerMilestone(score: Double): String {
    if (score < 37.2) {
        return "elevated"
    }
    if (score < 65.9) {
        return "watch"
    }
    if (score < 72.7) {
        return "ok"
    }
    if (score < 99.5) {
        return "critical"
    }
    return "high"
}
