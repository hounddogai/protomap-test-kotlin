package com.acme.transport.safety.scorecard.quota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun globalScorecardBudgetBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 44676.0, in order. */
fun globalScorecardBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44676.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeGlobalScorecardBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a global scorecard budget score falls in. */
fun classifyGlobalScorecardBudget(score: Double): String {
    if (score < 11.3) {
        return "ok"
    }
    if (score < 41.9) {
        return "high"
    }
    if (score < 80.2) {
        return "low"
    }
    if (score < 82.0) {
        return "watch"
    }
    return "critical"
}
