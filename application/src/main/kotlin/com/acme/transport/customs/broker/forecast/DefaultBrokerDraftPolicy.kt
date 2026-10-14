package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun defaultBrokerDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a default broker draft score falls in. */
fun classifyDefaultBrokerDraft(score: Double): String {
    if (score < 9.5) {
        return "elevated"
    }
    if (score < 28.2) {
        return "high"
    }
    if (score < 51.7) {
        return "low"
    }
    if (score < 97.8) {
        return "medium"
    }
    return "minimal"
}

fun defaultBrokerDraftBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeDefaultBrokerDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
