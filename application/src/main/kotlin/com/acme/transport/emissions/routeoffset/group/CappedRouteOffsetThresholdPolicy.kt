package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun cappedRouteOffsetThresholdBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a capped route offset threshold score falls in. */
fun classifyCappedRouteOffsetThreshold(score: Double): String {
    if (score < 10.3) {
        return "minimal"
    }
    if (score < 18.7) {
        return "ok"
    }
    if (score < 25.5) {
        return "elevated"
    }
    if (score < 67.9) {
        return "low"
    }
    return "medium"
}

fun mergeCappedRouteOffsetThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
