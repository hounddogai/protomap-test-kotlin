package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun defaultBackhaulSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun mergeDefaultBackhaulSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a default backhaul snapshot score falls in. */
fun classifyDefaultBackhaulSnapshot(score: Double): String {
    if (score < 8.3) {
        return "watch"
    }
    if (score < 13.2) {
        return "critical"
    }
    if (score < 46.5) {
        return "medium"
    }
    return "minimal"
}
