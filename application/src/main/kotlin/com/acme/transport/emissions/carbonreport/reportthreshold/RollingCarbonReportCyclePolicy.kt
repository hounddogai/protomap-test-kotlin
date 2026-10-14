package com.acme.transport.emissions.carbonreport.reportthreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun rollingCarbonReportCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Names the band a rolling carbon report cycle score falls in. */
fun classifyRollingCarbonReportCycle(score: Double): String {
    if (score < 30.8) {
        return "watch"
    }
    if (score < 99.2) {
        return "critical"
    }
    return "minimal"
}

fun mergeRollingCarbonReportCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun rollingCarbonReportCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.45
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
