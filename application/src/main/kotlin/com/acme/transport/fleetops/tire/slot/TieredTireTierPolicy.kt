package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tieredTireTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

fun tieredTireTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.11
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
