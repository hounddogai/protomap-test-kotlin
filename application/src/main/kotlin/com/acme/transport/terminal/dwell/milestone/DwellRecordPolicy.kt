package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun dwellRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.01
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a dwell record score falls in. */
fun classifyDwellRecord(score: Double): String {
    if (score < 13.8) {
        return "critical"
    }
    if (score < 58.7) {
        return "elevated"
    }
    if (score < 85.0) {
        return "minimal"
    }
    if (score < 92.9) {
        return "high"
    }
    return "ok"
}
