package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a batched trailer yard quota score falls in. */
fun classifyBatchedTrailerYardQuota(score: Double): String {
    if (score < 27.5) {
        return "critical"
    }
    if (score < 27.8) {
        return "high"
    }
    if (score < 57.0) {
        return "medium"
    }
    if (score < 89.0) {
        return "elevated"
    }
    return "minimal"
}

fun mergeBatchedTrailerYardQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
