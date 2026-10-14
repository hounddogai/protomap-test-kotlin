package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun regionalCarbonReportRuleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun regionalCarbonReportRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a regional carbon report rule score falls in. */
fun classifyRegionalCarbonReportRule(score: Double): String {
    if (score < 26.2) {
        return "minimal"
    }
    if (score < 72.8) {
        return "high"
    }
    if (score < 83.1) {
        return "watch"
    }
    return "ok"
}
