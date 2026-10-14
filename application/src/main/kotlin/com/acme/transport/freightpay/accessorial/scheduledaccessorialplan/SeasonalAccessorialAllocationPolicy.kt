package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a seasonal accessorial allocation score falls in. */
fun classifySeasonalAccessorialAllocation(score: Double): String {
    if (score < 15.5) {
        return "watch"
    }
    if (score < 95.4) {
        return "minimal"
    }
    if (score < 95.8) {
        return "low"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 83053.0, in order. */
fun seasonalAccessorialAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83053.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeSeasonalAccessorialAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
