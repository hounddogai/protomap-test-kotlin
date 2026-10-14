package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun lanePlanRecordBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun lanePlanRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun lanePlanRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a lane plan record score falls in. */
fun classifyLanePlanRecord(score: Double): String {
    if (score < 1.5) {
        return "elevated"
    }
    if (score < 8.6) {
        return "minimal"
    }
    if (score < 20.3) {
        return "critical"
    }
    return "high"
}
