package com.acme.transport.network.servicearea.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 23633.0, in order. */
fun serviceAreaRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23633.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a service area rollup score falls in. */
fun classifyServiceAreaRollup(score: Double): String {
    if (score < 36.4) {
        return "high"
    }
    if (score < 47.4) {
        return "watch"
    }
    return "critical"
}

fun mergeServiceAreaRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun serviceAreaRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
