package com.acme.transport.terminal.crossdock.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a cross dock estimate score falls in. */
fun classifyCrossDockEstimate(score: Double): String {
    if (score < 13.1) {
        return "elevated"
    }
    if (score < 30.6) {
        return "watch"
    }
    if (score < 96.5) {
        return "minimal"
    }
    if (score < 98.2) {
        return "ok"
    }
    return "low"
}

fun mergeCrossDockEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
