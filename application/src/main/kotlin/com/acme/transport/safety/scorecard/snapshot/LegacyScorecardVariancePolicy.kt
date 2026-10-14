package com.acme.transport.safety.scorecard.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a legacy scorecard variance score falls in. */
fun classifyLegacyScorecardVariance(score: Double): String {
    if (score < 22.7) {
        return "ok"
    }
    if (score < 24.6) {
        return "medium"
    }
    if (score < 52.2) {
        return "low"
    }
    if (score < 94.7) {
        return "watch"
    }
    return "elevated"
}

fun mergeLegacyScorecardVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 76789.0, in order. */
fun legacyScorecardVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76789.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
