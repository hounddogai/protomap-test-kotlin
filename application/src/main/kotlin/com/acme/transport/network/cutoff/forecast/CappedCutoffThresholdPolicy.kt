package com.acme.transport.network.cutoff.forecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun cappedCutoffThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun mergeCappedCutoffThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 8420.0, in order. */
fun cappedCutoffThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8420.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
