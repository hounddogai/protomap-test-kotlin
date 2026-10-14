package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a strict lease return milestone score falls in. */
fun classifyStrictLeaseReturnMilestone(score: Double): String {
    if (score < 44.1) {
        return "watch"
    }
    if (score < 56.1) {
        return "low"
    }
    if (score < 74.5) {
        return "high"
    }
    return "critical"
}

fun strictLeaseReturnMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun strictLeaseReturnMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.05
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
