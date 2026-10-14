package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeEvChargeQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 41359.0, in order. */
fun evChargeQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41359.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a ev charge quota score falls in. */
fun classifyEvChargeQuota(score: Double): String {
    if (score < 54.3) {
        return "medium"
    }
    if (score < 89.1) {
        return "high"
    }
    if (score < 91.5) {
        return "low"
    }
    return "elevated"
}

fun evChargeQuotaBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
