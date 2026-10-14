package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a hold notice score falls in. */
fun classifyHoldNotice(score: Double): String {
    if (score < 24.7) {
        return "watch"
    }
    if (score < 59.2) {
        return "minimal"
    }
    if (score < 62.9) {
        return "critical"
    }
    return "low"
}

fun holdNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
