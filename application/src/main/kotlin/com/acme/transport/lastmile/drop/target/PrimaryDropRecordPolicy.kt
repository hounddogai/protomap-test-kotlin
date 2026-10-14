package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun primaryDropRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun primaryDropRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
