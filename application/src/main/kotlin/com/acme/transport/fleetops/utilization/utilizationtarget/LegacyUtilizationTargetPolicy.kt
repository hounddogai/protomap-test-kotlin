package com.acme.transport.fleetops.utilization.utilizationtarget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun legacyUtilizationTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.04
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a legacy utilization target score falls in. */
fun classifyLegacyUtilizationTarget(score: Double): String {
    if (score < 17.6) {
        return "minimal"
    }
    if (score < 51.3) {
        return "critical"
    }
    if (score < 75.2) {
        return "low"
    }
    return "medium"
}

fun legacyUtilizationTargetBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
