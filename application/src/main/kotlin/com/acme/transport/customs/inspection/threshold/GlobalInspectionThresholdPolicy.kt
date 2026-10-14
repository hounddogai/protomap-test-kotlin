package com.acme.transport.customs.inspection.threshold

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun globalInspectionThresholdBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun globalInspectionThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.4
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
