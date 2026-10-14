package com.acme.transport.crewing.swap.swapclaim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 68777.0, in order. */
fun cappedSwapClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68777.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedSwapClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun mergeCappedSwapClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a capped swap claim score falls in. */
fun classifyCappedSwapClaim(score: Double): String {
    if (score < 9.5) {
        return "minimal"
    }
    if (score < 36.0) {
        return "low"
    }
    if (score < 60.4) {
        return "high"
    }
    return "critical"
}
