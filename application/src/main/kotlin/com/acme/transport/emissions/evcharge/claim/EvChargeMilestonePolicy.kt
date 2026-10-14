package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun evChargeMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun evChargeMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a ev charge milestone score falls in. */
fun classifyEvChargeMilestone(score: Double): String {
    if (score < 47.5) {
        return "elevated"
    }
    if (score < 62.7) {
        return "critical"
    }
    if (score < 82.8) {
        return "ok"
    }
    return "watch"
}
