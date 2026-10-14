package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun cutoffScoreBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun cutoffScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
