package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a regional service area batch score falls in. */
fun classifyRegionalServiceAreaBatch(score: Double): String {
    if (score < 30.2) {
        return "high"
    }
    if (score < 61.2) {
        return "minimal"
    }
    if (score < 79.9) {
        return "elevated"
    }
    return "low"
}

fun regionalServiceAreaBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Counts how many amounts fit in a budget of 74334.0, in order. */
fun regionalServiceAreaBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74334.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeRegionalServiceAreaBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
