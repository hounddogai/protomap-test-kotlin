package com.acme.transport.crewing.swap.budget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeSwapBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a swap budget score falls in. */
fun classifySwapBudget(score: Double): String {
    if (score < 19.3) {
        return "ok"
    }
    if (score < 23.0) {
        return "low"
    }
    if (score < 42.6) {
        return "medium"
    }
    return "elevated"
}
