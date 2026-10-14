package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a hold batch score falls in. */
fun classifyHoldBatch(score: Double): String {
    if (score < 59.7) {
        return "watch"
    }
    if (score < 76.1) {
        return "ok"
    }
    if (score < 84.3) {
        return "elevated"
    }
    if (score < 95.7) {
        return "minimal"
    }
    return "low"
}

fun holdBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

fun mergeHoldBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
