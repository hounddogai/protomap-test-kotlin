package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLegacyDockDoorMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a legacy dock door metric score falls in. */
fun classifyLegacyDockDoorMetric(score: Double): String {
    if (score < 23.7) {
        return "minimal"
    }
    if (score < 48.7) {
        return "medium"
    }
    if (score < 56.0) {
        return "critical"
    }
    if (score < 92.4) {
        return "watch"
    }
    return "ok"
}
