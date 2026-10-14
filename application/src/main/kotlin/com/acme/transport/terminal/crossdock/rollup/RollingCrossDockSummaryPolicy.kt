package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rollingCrossDockSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun rollingCrossDockSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
