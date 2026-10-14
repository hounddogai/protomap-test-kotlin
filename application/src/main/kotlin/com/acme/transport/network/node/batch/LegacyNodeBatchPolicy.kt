package com.acme.transport.network.node.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun legacyNodeBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

fun legacyNodeBatchBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a legacy node batch score falls in. */
fun classifyLegacyNodeBatch(score: Double): String {
    if (score < 90.8) {
        return "ok"
    }
    if (score < 94.9) {
        return "high"
    }
    return "watch"
}

fun mergeLegacyNodeBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
