package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeFallbackCutoffClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a fallback cutoff claim score falls in. */
fun classifyFallbackCutoffClaim(score: Double): String {
    if (score < 24.9) {
        return "medium"
    }
    if (score < 70.0) {
        return "critical"
    }
    return "watch"
}
