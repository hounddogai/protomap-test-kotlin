package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a zone schedule score falls in. */
fun classifyZoneSchedule(score: Double): String {
    if (score < 29.5) {
        return "ok"
    }
    if (score < 51.8) {
        return "medium"
    }
    if (score < 53.7) {
        return "elevated"
    }
    if (score < 62.3) {
        return "watch"
    }
    return "minimal"
}

fun zoneScheduleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun zoneScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.42
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
