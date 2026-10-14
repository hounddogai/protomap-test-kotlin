package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a global scorecard manifest score falls in. */
fun classifyGlobalScorecardManifest(score: Double): String {
    if (score < 34.0) {
        return "medium"
    }
    if (score < 44.7) {
        return "minimal"
    }
    if (score < 74.7) {
        return "ok"
    }
    if (score < 76.7) {
        return "high"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 37958.0, in order. */
fun globalScorecardManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37958.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeGlobalScorecardManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
