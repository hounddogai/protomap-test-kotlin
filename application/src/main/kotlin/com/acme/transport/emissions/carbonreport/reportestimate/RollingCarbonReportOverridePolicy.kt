package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rollingCarbonReportOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun rollingCarbonReportOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a rolling carbon report override score falls in. */
fun classifyRollingCarbonReportOverride(score: Double): String {
    if (score < 48.9) {
        return "watch"
    }
    if (score < 61.6) {
        return "ok"
    }
    return "low"
}

/** Puts a rolling carbon report override code in its canonical RCRO- form. */
fun normalizeRollingCarbonReportOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCRO-")) {
        return clean
    }
    return "RCRO-${clean.replace(" ", "-")}"
}
