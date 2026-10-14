package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a route offset plan score falls in. */
fun classifyRouteOffsetPlan(score: Double): String {
    if (score < 6.4) {
        return "critical"
    }
    if (score < 61.3) {
        return "minimal"
    }
    return "high"
}

fun routeOffsetPlanBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun routeOffsetPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
