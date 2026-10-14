package com.acme.transport.fleetops.tire.budget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun rollingTireTargetBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a rolling tire target score falls in. */
fun classifyRollingTireTarget(score: Double): String {
    if (score < 2.2) {
        return "high"
    }
    if (score < 56.9) {
        return "critical"
    }
    if (score < 64.8) {
        return "watch"
    }
    return "minimal"
}

fun rollingTireTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a rolling tire target code in its canonical RTT- form. */
fun normalizeRollingTireTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTT-")) {
        return clean
    }
    return "RTT-${clean.replace(" ", "-")}"
}
