package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun routeOffsetCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun routeOffsetCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
