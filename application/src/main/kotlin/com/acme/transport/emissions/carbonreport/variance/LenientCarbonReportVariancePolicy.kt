package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a lenient carbon report variance score falls in. */
fun classifyLenientCarbonReportVariance(score: Double): String {
    if (score < 20.3) {
        return "watch"
    }
    if (score < 66.7) {
        return "elevated"
    }
    return "low"
}

fun lenientCarbonReportVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
