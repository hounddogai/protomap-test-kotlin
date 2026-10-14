package com.acme.transport.freightpay.dispute.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeDisputeWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a dispute window score falls in. */
fun classifyDisputeWindow(score: Double): String {
    if (score < 30.7) {
        return "minimal"
    }
    if (score < 33.6) {
        return "high"
    }
    if (score < 39.7) {
        return "low"
    }
    if (score < 69.5) {
        return "watch"
    }
    return "elevated"
}

fun disputeWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
