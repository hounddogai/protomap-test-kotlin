package com.acme.transport.network.cutoff.baseline

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalCutoffReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.72
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a global cutoff reservation score falls in. */
fun classifyGlobalCutoffReservation(score: Double): String {
    if (score < 37.4) {
        return "critical"
    }
    if (score < 61.3) {
        return "medium"
    }
    if (score < 86.5) {
        return "watch"
    }
    if (score < 87.8) {
        return "high"
    }
    return "elevated"
}
