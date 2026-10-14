package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRollingSwapTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a rolling swap target score falls in. */
fun classifyRollingSwapTarget(score: Double): String {
    if (score < 28.1) {
        return "watch"
    }
    if (score < 35.0) {
        return "ok"
    }
    if (score < 74.1) {
        return "high"
    }
    return "critical"
}
