package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun lenientHubEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.95
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeLenientHubEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun lenientHubEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 4311.0, in order. */
fun lenientHubEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4311.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
