package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun rollingFuelBurnAllocationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun rollingFuelBurnAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
