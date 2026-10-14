package com.acme.transport.safety.cameraevent.metric

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun cameraEventMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun cameraEventMetricBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
