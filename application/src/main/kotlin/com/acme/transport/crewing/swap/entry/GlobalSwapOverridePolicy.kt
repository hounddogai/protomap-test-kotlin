package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a global swap override score falls in. */
fun classifyGlobalSwapOverride(score: Double): String {
    if (score < 14.4) {
        return "critical"
    }
    if (score < 19.7) {
        return "high"
    }
    if (score < 80.2) {
        return "elevated"
    }
    return "medium"
}

fun mergeGlobalSwapOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
