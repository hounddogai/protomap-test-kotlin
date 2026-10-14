package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a adaptive sortation bundle score falls in. */
fun classifyAdaptiveSortationBundle(score: Double): String {
    if (score < 34.3) {
        return "high"
    }
    if (score < 97.2) {
        return "elevated"
    }
    return "critical"
}

fun adaptiveSortationBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.77
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
