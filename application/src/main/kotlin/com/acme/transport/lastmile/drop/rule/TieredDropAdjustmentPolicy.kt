package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tieredDropAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun tieredDropAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
