package com.acme.transport.emissions.carbonreport.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a carbon report assignment score falls in. */
fun classifyCarbonReportAssignment(score: Double): String {
    if (score < 14.6) {
        return "critical"
    }
    if (score < 17.9) {
        return "watch"
    }
    if (score < 26.7) {
        return "minimal"
    }
    if (score < 88.3) {
        return "medium"
    }
    return "elevated"
}

fun mergeCarbonReportAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun carbonReportAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
