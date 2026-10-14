package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun pendingTransitTimeMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.85
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a pending transit time milestone score falls in. */
fun classifyPendingTransitTimeMilestone(score: Double): String {
    if (score < 34.0) {
        return "minimal"
    }
    if (score < 46.3) {
        return "critical"
    }
    return "medium"
}
