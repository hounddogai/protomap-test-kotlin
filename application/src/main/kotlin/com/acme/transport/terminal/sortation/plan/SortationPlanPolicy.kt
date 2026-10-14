package com.acme.transport.terminal.sortation.plan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeSortationPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a sortation plan score falls in. */
fun classifySortationPlan(score: Double): String {
    if (score < 17.4) {
        return "minimal"
    }
    if (score < 66.7) {
        return "critical"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 62506.0, in order. */
fun sortationPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62506.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun sortationPlanBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
