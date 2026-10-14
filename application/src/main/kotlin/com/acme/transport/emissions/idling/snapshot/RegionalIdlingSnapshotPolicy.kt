package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRegionalIdlingSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a regional idling snapshot score falls in. */
fun classifyRegionalIdlingSnapshot(score: Double): String {
    if (score < 12.7) {
        return "low"
    }
    if (score < 70.9) {
        return "medium"
    }
    return "critical"
}

fun regionalIdlingSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
