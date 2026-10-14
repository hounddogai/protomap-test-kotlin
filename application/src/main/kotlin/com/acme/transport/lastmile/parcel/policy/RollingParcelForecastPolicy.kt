package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a rolling parcel forecast score falls in. */
fun classifyRollingParcelForecast(score: Double): String {
    if (score < 10.1) {
        return "critical"
    }
    if (score < 30.5) {
        return "high"
    }
    if (score < 52.8) {
        return "elevated"
    }
    if (score < 99.5) {
        return "watch"
    }
    return "ok"
}

fun rollingParcelForecastBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun rollingParcelForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.57
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
