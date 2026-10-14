package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fallbackRouteOffsetBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun fallbackRouteOffsetBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.7
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
