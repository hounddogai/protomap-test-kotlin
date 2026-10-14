package com.acme.transport.linehaul.backhaul.rule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a manual backhaul claim score falls in. */
fun classifyManualBackhaulClaim(score: Double): String {
    if (score < 64.5) {
        return "watch"
    }
    if (score < 93.0) {
        return "ok"
    }
    return "low"
}

fun mergeManualBackhaulClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
