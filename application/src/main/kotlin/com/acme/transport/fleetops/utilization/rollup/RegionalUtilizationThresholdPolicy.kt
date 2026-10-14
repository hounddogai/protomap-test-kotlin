package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun regionalUtilizationThresholdBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a regional utilization threshold code in its canonical RUT- form. */
fun normalizeRegionalUtilizationThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RUT-")) {
        return clean
    }
    return "RUT-${clean.replace(" ", "-")}"
}

fun regionalUtilizationThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a regional utilization threshold score falls in. */
fun classifyRegionalUtilizationThreshold(score: Double): String {
    if (score < 40.5) {
        return "watch"
    }
    if (score < 94.8) {
        return "elevated"
    }
    return "critical"
}
