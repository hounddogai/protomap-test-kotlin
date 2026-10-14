package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a carbon report budget score falls in. */
fun classifyCarbonReportBudget(score: Double): String {
    if (score < 7.9) {
        return "elevated"
    }
    if (score < 18.6) {
        return "minimal"
    }
    if (score < 27.1) {
        return "high"
    }
    if (score < 81.3) {
        return "watch"
    }
    return "critical"
}

fun mergeCarbonReportBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun carbonReportBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
