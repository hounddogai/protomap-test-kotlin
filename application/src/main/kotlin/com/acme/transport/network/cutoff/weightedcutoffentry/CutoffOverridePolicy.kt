package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a cutoff override score falls in. */
fun classifyCutoffOverride(score: Double): String {
    if (score < 4.1) {
        return "minimal"
    }
    if (score < 12.2) {
        return "critical"
    }
    return "elevated"
}

fun mergeCutoffOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
