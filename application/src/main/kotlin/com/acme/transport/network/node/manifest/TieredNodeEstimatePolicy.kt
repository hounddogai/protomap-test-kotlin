package com.acme.transport.network.node.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun tieredNodeEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun mergeTieredNodeEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a tiered node estimate score falls in. */
fun classifyTieredNodeEstimate(score: Double): String {
    if (score < 13.3) {
        return "minimal"
    }
    if (score < 65.5) {
        return "ok"
    }
    if (score < 91.6) {
        return "elevated"
    }
    return "low"
}
