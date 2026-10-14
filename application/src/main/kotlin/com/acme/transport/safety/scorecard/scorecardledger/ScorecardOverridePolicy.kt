package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a scorecard override score falls in. */
fun classifyScorecardOverride(score: Double): String {
    if (score < 15.6) {
        return "medium"
    }
    if (score < 92.7) {
        return "minimal"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 79836.0, in order. */
fun scorecardOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79836.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeScorecardOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
