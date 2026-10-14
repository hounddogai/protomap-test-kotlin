package com.acme.transport.network.transittime.allocation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun defaultTransitTimeGroupBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun defaultTransitTimeGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.66
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
