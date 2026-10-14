package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a legacy lane plan slot score falls in. */
fun classifyLegacyLanePlanSlot(score: Double): String {
    if (score < 9.2) {
        return "minimal"
    }
    if (score < 12.0) {
        return "high"
    }
    if (score < 61.7) {
        return "medium"
    }
    if (score < 68.6) {
        return "ok"
    }
    return "watch"
}

fun legacyLanePlanSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun legacyLanePlanSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
