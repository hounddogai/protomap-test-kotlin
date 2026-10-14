package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeRegionalCarbonReportManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a regional carbon report manifest score falls in. */
fun classifyRegionalCarbonReportManifest(score: Double): String {
    if (score < 7.3) {
        return "watch"
    }
    if (score < 38.3) {
        return "elevated"
    }
    if (score < 91.3) {
        return "medium"
    }
    return "minimal"
}

fun regionalCarbonReportManifestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
