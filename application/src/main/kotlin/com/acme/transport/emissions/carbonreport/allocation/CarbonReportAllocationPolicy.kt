package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeCarbonReportAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun carbonReportAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun carbonReportAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a carbon report allocation score falls in. */
fun classifyCarbonReportAllocation(score: Double): String {
    if (score < 37.8) {
        return "low"
    }
    if (score < 43.2) {
        return "medium"
    }
    if (score < 61.4) {
        return "ok"
    }
    if (score < 85.6) {
        return "watch"
    }
    return "minimal"
}
