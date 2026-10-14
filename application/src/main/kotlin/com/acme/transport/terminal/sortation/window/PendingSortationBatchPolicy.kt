package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergePendingSortationBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun pendingSortationBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a pending sortation batch score falls in. */
fun classifyPendingSortationBatch(score: Double): String {
    if (score < 10.3) {
        return "ok"
    }
    if (score < 82.0) {
        return "critical"
    }
    if (score < 95.1) {
        return "medium"
    }
    return "low"
}
