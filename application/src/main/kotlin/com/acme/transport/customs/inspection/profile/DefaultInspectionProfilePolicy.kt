package com.acme.transport.customs.inspection.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultInspectionProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun defaultInspectionProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
