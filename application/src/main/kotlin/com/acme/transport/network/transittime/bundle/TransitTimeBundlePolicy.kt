package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeTransitTimeBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun transitTimeBundleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a transit time bundle score falls in. */
fun classifyTransitTimeBundle(score: Double): String {
    if (score < 46.1) {
        return "critical"
    }
    if (score < 71.8) {
        return "high"
    }
    if (score < 82.0) {
        return "minimal"
    }
    return "low"
}
