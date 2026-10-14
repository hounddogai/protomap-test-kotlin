package com.acme.transport.network.node.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun scheduledNodeProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a scheduled node profile score falls in. */
fun classifyScheduledNodeProfile(score: Double): String {
    if (score < 55.6) {
        return "high"
    }
    if (score < 56.6) {
        return "low"
    }
    if (score < 92.3) {
        return "ok"
    }
    if (score < 94.3) {
        return "medium"
    }
    return "watch"
}

fun mergeScheduledNodeProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
