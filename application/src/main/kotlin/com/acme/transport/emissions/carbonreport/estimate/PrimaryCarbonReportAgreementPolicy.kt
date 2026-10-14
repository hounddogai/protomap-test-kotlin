package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a primary carbon report agreement score falls in. */
fun classifyPrimaryCarbonReportAgreement(score: Double): String {
    if (score < 12.2) {
        return "watch"
    }
    if (score < 13.2) {
        return "high"
    }
    if (score < 71.5) {
        return "minimal"
    }
    return "ok"
}

fun primaryCarbonReportAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun primaryCarbonReportAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
