package com.acme.transport.network.hub.batchedhubmilestone

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun lenientHubThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun lenientHubThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun lenientHubThresholdBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
