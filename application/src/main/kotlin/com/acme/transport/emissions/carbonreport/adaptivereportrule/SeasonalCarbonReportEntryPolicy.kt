package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a seasonal carbon report entry score falls in. */
fun classifySeasonalCarbonReportEntry(score: Double): String {
    if (score < 3.3) {
        return "high"
    }
    if (score < 10.5) {
        return "elevated"
    }
    if (score < 46.5) {
        return "critical"
    }
    if (score < 76.7) {
        return "ok"
    }
    return "watch"
}

fun mergeSeasonalCarbonReportEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
