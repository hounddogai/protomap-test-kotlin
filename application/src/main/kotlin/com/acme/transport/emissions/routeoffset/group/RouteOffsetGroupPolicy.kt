package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun routeOffsetGroupBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a route offset group score falls in. */
fun classifyRouteOffsetGroup(score: Double): String {
    if (score < 21.9) {
        return "critical"
    }
    if (score < 60.8) {
        return "low"
    }
    if (score < 90.4) {
        return "watch"
    }
    if (score < 95.5) {
        return "ok"
    }
    return "medium"
}

fun routeOffsetGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.59
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
