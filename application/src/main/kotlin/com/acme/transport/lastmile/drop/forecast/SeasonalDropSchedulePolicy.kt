package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun seasonalDropScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun seasonalDropScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
