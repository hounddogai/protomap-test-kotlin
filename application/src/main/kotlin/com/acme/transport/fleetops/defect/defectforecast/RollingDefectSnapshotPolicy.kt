package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a rolling defect snapshot score falls in. */
fun classifyRollingDefectSnapshot(score: Double): String {
    if (score < 33.2) {
        return "minimal"
    }
    if (score < 88.8) {
        return "high"
    }
    if (score < 96.6) {
        return "medium"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 53031.0, in order. */
fun rollingDefectSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53031.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeRollingDefectSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
