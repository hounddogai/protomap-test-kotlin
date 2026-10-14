package com.acme.transport.safety.cameraevent.eventadjustment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalCameraEventAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun regionalCameraEventAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
