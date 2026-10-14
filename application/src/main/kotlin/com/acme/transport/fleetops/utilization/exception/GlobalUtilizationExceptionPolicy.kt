package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeGlobalUtilizationExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a global utilization exception score falls in. */
fun classifyGlobalUtilizationException(score: Double): String {
    if (score < 36.6) {
        return "medium"
    }
    if (score < 44.8) {
        return "critical"
    }
    if (score < 71.2) {
        return "watch"
    }
    if (score < 82.9) {
        return "minimal"
    }
    return "high"
}

fun globalUtilizationExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
