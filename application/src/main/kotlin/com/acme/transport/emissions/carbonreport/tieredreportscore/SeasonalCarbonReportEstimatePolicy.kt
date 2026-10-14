package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a seasonal carbon report estimate score falls in. */
fun classifySeasonalCarbonReportEstimate(score: Double): String {
    if (score < 72.7) {
        return "medium"
    }
    if (score < 95.9) {
        return "low"
    }
    return "ok"
}

fun mergeSeasonalCarbonReportEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun seasonalCarbonReportEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
