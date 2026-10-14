package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalAvailabilityGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun regionalAvailabilityGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
