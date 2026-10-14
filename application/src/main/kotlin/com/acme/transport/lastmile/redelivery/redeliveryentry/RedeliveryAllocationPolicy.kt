package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a redelivery allocation score falls in. */
fun classifyRedeliveryAllocation(score: Double): String {
    if (score < 12.3) {
        return "watch"
    }
    if (score < 58.7) {
        return "minimal"
    }
    if (score < 63.2) {
        return "ok"
    }
    if (score < 88.0) {
        return "high"
    }
    return "elevated"
}

fun mergeRedeliveryAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 8155.0, in order. */
fun redeliveryAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8155.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
