package com.acme.transport.network.node.assignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeAdaptiveNodeAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a adaptive node allocation score falls in. */
fun classifyAdaptiveNodeAllocation(score: Double): String {
    if (score < 70.9) {
        return "low"
    }
    if (score < 80.3) {
        return "critical"
    }
    if (score < 91.1) {
        return "ok"
    }
    return "watch"
}
