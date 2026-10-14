package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergePrimaryRouteOffsetBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun primaryRouteOffsetBudgetBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a primary route offset budget score falls in. */
fun classifyPrimaryRouteOffsetBudget(score: Double): String {
    if (score < 28.6) {
        return "minimal"
    }
    if (score < 42.5) {
        return "elevated"
    }
    return "watch"
}
