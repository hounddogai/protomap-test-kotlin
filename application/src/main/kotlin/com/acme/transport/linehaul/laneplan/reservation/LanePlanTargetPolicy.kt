package com.acme.transport.linehaul.laneplan.reservation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a lane plan target score falls in. */
fun classifyLanePlanTarget(score: Double): String {
    if (score < 6.9) {
        return "medium"
    }
    if (score < 17.0) {
        return "watch"
    }
    if (score < 89.6) {
        return "elevated"
    }
    return "low"
}

fun lanePlanTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun lanePlanTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
