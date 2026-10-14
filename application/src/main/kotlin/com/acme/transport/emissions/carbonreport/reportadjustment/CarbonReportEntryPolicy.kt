package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeCarbonReportEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun carbonReportEntryBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a carbon report entry score falls in. */
fun classifyCarbonReportEntry(score: Double): String {
    if (score < 32.7) {
        return "critical"
    }
    if (score < 40.2) {
        return "watch"
    }
    if (score < 81.4) {
        return "elevated"
    }
    return "ok"
}
