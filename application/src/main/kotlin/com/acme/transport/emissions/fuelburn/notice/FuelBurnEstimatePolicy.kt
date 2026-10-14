package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun fuelBurnEstimateBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun mergeFuelBurnEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a fuel burn estimate score falls in. */
fun classifyFuelBurnEstimate(score: Double): String {
    if (score < 45.1) {
        return "medium"
    }
    if (score < 58.9) {
        return "low"
    }
    return "watch"
}
