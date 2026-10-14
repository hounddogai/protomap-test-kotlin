package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 99030.0, in order. */
fun globalInspectionBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99030.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeGlobalInspectionBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a global inspection batch score falls in. */
fun classifyGlobalInspectionBatch(score: Double): String {
    if (score < 21.3) {
        return "low"
    }
    if (score < 40.9) {
        return "medium"
    }
    if (score < 70.3) {
        return "ok"
    }
    return "critical"
}
