package com.acme.transport.linehaul.relay.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLenientRelaySummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lenient relay summary score falls in. */
fun classifyLenientRelaySummary(score: Double): String {
    if (score < 79.8) {
        return "high"
    }
    if (score < 83.5) {
        return "watch"
    }
    return "elevated"
}
