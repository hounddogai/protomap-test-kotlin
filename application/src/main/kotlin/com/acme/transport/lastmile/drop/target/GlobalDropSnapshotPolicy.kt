package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeGlobalDropSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a global drop snapshot score falls in. */
fun classifyGlobalDropSnapshot(score: Double): String {
    if (score < 22.2) {
        return "ok"
    }
    if (score < 57.2) {
        return "medium"
    }
    return "low"
}

fun globalDropSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
