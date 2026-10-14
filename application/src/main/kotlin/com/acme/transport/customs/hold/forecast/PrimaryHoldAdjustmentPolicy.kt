package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a primary hold adjustment score falls in. */
fun classifyPrimaryHoldAdjustment(score: Double): String {
    if (score < 14.0) {
        return "watch"
    }
    if (score < 16.1) {
        return "elevated"
    }
    return "ok"
}

fun primaryHoldAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
