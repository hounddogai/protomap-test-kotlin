package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a emission factor snapshot score falls in. */
fun classifyEmissionFactorSnapshot(score: Double): String {
    if (score < 58.1) {
        return "medium"
    }
    if (score < 61.5) {
        return "minimal"
    }
    if (score < 90.9) {
        return "high"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 64555.0, in order. */
fun emissionFactorSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64555.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeEmissionFactorSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
