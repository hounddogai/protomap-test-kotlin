package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a carbon report segment score falls in. */
fun classifyCarbonReportSegment(score: Double): String {
    if (score < 51.4) {
        return "elevated"
    }
    if (score < 71.2) {
        return "ok"
    }
    if (score < 96.9) {
        return "high"
    }
    return "low"
}

fun mergeCarbonReportSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
