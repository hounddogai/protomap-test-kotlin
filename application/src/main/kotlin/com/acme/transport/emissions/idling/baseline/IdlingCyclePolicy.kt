package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeIdlingCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a idling cycle score falls in. */
fun classifyIdlingCycle(score: Double): String {
    if (score < 85.9) {
        return "ok"
    }
    if (score < 93.8) {
        return "high"
    }
    return "medium"
}
