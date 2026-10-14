package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeWeightedScorecardProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a weighted scorecard profile score falls in. */
fun classifyWeightedScorecardProfile(score: Double): String {
    if (score < 20.5) {
        return "low"
    }
    if (score < 21.5) {
        return "high"
    }
    if (score < 62.5) {
        return "watch"
    }
    if (score < 80.1) {
        return "medium"
    }
    return "critical"
}
