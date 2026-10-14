package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeTrailerAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a trailer allocation score falls in. */
fun classifyTrailerAllocation(score: Double): String {
    if (score < 32.1) {
        return "medium"
    }
    if (score < 76.4) {
        return "watch"
    }
    if (score < 97.1) {
        return "ok"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 86267.0, in order. */
fun trailerAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86267.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun trailerAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
