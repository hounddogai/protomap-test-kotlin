package com.acme.transport.fleetops.utilization.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 6602.0, in order. */
fun manualUtilizationVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6602.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeManualUtilizationVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun manualUtilizationVarianceBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a manual utilization variance score falls in. */
fun classifyManualUtilizationVariance(score: Double): String {
    if (score < 45.3) {
        return "minimal"
    }
    if (score < 63.0) {
        return "elevated"
    }
    return "medium"
}
