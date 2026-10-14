package com.acme.transport.network.node.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a fallback node metric score falls in. */
fun classifyFallbackNodeMetric(score: Double): String {
    if (score < 61.5) {
        return "medium"
    }
    if (score < 91.3) {
        return "watch"
    }
    return "ok"
}

fun mergeFallbackNodeMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
