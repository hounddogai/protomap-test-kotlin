package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 6710.0, in order. */
fun weightedDefectManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6710.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a weighted defect manifest score falls in. */
fun classifyWeightedDefectManifest(score: Double): String {
    if (score < 53.9) {
        return "critical"
    }
    if (score < 55.3) {
        return "medium"
    }
    if (score < 58.0) {
        return "minimal"
    }
    return "low"
}

fun mergeWeightedDefectManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
