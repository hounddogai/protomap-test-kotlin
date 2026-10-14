package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeScheduledHubCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun scheduledHubCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a scheduled hub checkpoint score falls in. */
fun classifyScheduledHubCheckpoint(score: Double): String {
    if (score < 73.8) {
        return "medium"
    }
    if (score < 74.3) {
        return "watch"
    }
    if (score < 81.3) {
        return "ok"
    }
    return "minimal"
}
