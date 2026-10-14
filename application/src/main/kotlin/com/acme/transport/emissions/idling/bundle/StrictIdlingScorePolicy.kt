package com.acme.transport.emissions.idling.bundle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun strictIdlingScoreBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun strictIdlingScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.19
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
