package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a regional scorecard threshold score falls in. */
fun classifyRegionalScorecardThreshold(score: Double): String {
    if (score < 3.7) {
        return "high"
    }
    if (score < 62.3) {
        return "elevated"
    }
    if (score < 63.6) {
        return "low"
    }
    if (score < 68.9) {
        return "ok"
    }
    return "minimal"
}

fun mergeRegionalScorecardThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 52699.0, in order. */
fun regionalScorecardThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 52699.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
