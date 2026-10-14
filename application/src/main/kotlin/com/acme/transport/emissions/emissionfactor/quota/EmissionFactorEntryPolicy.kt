package com.acme.transport.emissions.emissionfactor.quota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeEmissionFactorEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a emission factor entry score falls in. */
fun classifyEmissionFactorEntry(score: Double): String {
    if (score < 17.1) {
        return "elevated"
    }
    if (score < 18.8) {
        return "critical"
    }
    if (score < 52.0) {
        return "low"
    }
    if (score < 69.8) {
        return "high"
    }
    return "ok"
}

fun emissionFactorEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
