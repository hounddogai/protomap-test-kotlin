package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun relayVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a relay variance score falls in. */
fun classifyRelayVariance(score: Double): String {
    if (score < 31.5) {
        return "watch"
    }
    if (score < 58.7) {
        return "medium"
    }
    if (score < 90.9) {
        return "minimal"
    }
    if (score < 97.3) {
        return "low"
    }
    return "elevated"
}

fun mergeRelayVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
