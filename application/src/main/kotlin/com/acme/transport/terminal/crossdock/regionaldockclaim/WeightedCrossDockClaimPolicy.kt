package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a weighted cross dock claim score falls in. */
fun classifyWeightedCrossDockClaim(score: Double): String {
    if (score < 43.6) {
        return "medium"
    }
    if (score < 51.7) {
        return "watch"
    }
    if (score < 61.6) {
        return "critical"
    }
    if (score < 64.6) {
        return "high"
    }
    return "ok"
}

fun mergeWeightedCrossDockClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun weightedCrossDockClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
