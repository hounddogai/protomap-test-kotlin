package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a relay batch score falls in. */
fun classifyRelayBatch(score: Double): String {
    if (score < 19.6) {
        return "elevated"
    }
    if (score < 23.6) {
        return "medium"
    }
    if (score < 60.4) {
        return "critical"
    }
    if (score < 65.9) {
        return "watch"
    }
    return "minimal"
}

fun relayBatchBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun mergeRelayBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
