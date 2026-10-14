package com.acme.transport.customs.clearance.threshold

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun clearanceThresholdBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun clearanceThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.57
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
