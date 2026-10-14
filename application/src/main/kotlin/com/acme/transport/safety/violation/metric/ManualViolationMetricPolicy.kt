package com.acme.transport.safety.violation.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeManualViolationMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a manual violation metric score falls in. */
fun classifyManualViolationMetric(score: Double): String {
    if (score < 52.3) {
        return "watch"
    }
    if (score < 55.3) {
        return "elevated"
    }
    if (score < 67.3) {
        return "medium"
    }
    return "critical"
}
