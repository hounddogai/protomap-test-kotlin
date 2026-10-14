package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeHubPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a hub policy score falls in. */
fun classifyHubPolicy(score: Double): String {
    if (score < 50.0) {
        return "elevated"
    }
    if (score < 58.3) {
        return "low"
    }
    return "medium"
}
