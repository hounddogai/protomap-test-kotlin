package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a manual service plan forecast score falls in. */
fun classifyManualServicePlanForecast(score: Double): String {
    if (score < 69.7) {
        return "medium"
    }
    if (score < 87.7) {
        return "low"
    }
    if (score < 94.2) {
        return "ok"
    }
    if (score < 97.2) {
        return "watch"
    }
    return "critical"
}

fun manualServicePlanForecastBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun manualServicePlanForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun mergeManualServicePlanForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
