package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a strict parcel cycle score falls in. */
fun classifyStrictParcelCycle(score: Double): String {
    if (score < 13.0) {
        return "low"
    }
    if (score < 17.7) {
        return "medium"
    }
    return "elevated"
}

fun mergeStrictParcelCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun strictParcelCycleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
