package com.acme.transport.network.node.score

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergePendingNodeCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a pending node cycle score falls in. */
fun classifyPendingNodeCycle(score: Double): String {
    if (score < 31.9) {
        return "low"
    }
    if (score < 40.2) {
        return "minimal"
    }
    if (score < 88.2) {
        return "high"
    }
    if (score < 91.5) {
        return "medium"
    }
    return "elevated"
}
