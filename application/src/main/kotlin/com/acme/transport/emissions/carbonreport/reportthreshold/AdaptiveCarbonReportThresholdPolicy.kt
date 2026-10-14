package com.acme.transport.emissions.carbonreport.reportthreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun adaptiveCarbonReportThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun mergeAdaptiveCarbonReportThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a adaptive carbon report threshold score falls in. */
fun classifyAdaptiveCarbonReportThreshold(score: Double): String {
    if (score < 14.6) {
        return "elevated"
    }
    if (score < 30.5) {
        return "minimal"
    }
    if (score < 52.2) {
        return "low"
    }
    return "high"
}
