package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a primary hold override score falls in. */
fun classifyPrimaryHoldOverride(score: Double): String {
    if (score < 11.1) {
        return "watch"
    }
    if (score < 27.7) {
        return "critical"
    }
    if (score < 53.0) {
        return "ok"
    }
    return "minimal"
}

fun primaryHoldOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
