package com.acme.transport.crewing.qualification.qualificationoverride

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a adaptive qualification override score falls in. */
fun classifyAdaptiveQualificationOverride(score: Double): String {
    if (score < 51.5) {
        return "medium"
    }
    if (score < 79.4) {
        return "critical"
    }
    return "high"
}

fun adaptiveQualificationOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun adaptiveQualificationOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
