package com.acme.transport.fleetops.defect.template

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveDefectTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun adaptiveDefectTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a adaptive defect template score falls in. */
fun classifyAdaptiveDefectTemplate(score: Double): String {
    if (score < 68.6) {
        return "minimal"
    }
    if (score < 92.0) {
        return "medium"
    }
    return "low"
}
