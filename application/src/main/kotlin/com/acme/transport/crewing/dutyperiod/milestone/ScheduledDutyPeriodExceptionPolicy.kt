package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun scheduledDutyPeriodExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scheduled duty period exception score falls in. */
fun classifyScheduledDutyPeriodException(score: Double): String {
    if (score < 29.1) {
        return "ok"
    }
    if (score < 78.6) {
        return "minimal"
    }
    return "medium"
}
