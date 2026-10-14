package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeAdaptiveTrailerSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a adaptive trailer summary score falls in. */
fun classifyAdaptiveTrailerSummary(score: Double): String {
    if (score < 26.0) {
        return "medium"
    }
    if (score < 52.3) {
        return "elevated"
    }
    if (score < 80.4) {
        return "low"
    }
    return "ok"
}

fun adaptiveTrailerSummaryBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
