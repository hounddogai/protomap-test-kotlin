package com.acme.transport.customs.broker.rollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun strictBrokerScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a strict broker schedule score falls in. */
fun classifyStrictBrokerSchedule(score: Double): String {
    if (score < 47.1) {
        return "medium"
    }
    if (score < 65.2) {
        return "elevated"
    }
    if (score < 79.0) {
        return "minimal"
    }
    return "critical"
}
