package com.acme.transport.freightpay.accessorial.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeDefaultAccessorialRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a default accessorial rollup score falls in. */
fun classifyDefaultAccessorialRollup(score: Double): String {
    if (score < 2.8) {
        return "high"
    }
    if (score < 16.5) {
        return "watch"
    }
    if (score < 87.8) {
        return "minimal"
    }
    return "low"
}

fun defaultAccessorialRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
