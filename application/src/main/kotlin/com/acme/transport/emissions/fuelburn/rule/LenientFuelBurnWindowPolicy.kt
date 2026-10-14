package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun lenientFuelBurnWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun lenientFuelBurnWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
