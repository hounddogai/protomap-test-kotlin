package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeInspectionEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun inspectionEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a inspection estimate score falls in. */
fun classifyInspectionEstimate(score: Double): String {
    if (score < 9.4) {
        return "minimal"
    }
    if (score < 90.5) {
        return "ok"
    }
    if (score < 98.1) {
        return "high"
    }
    if (score < 99.4) {
        return "watch"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 10586.0, in order. */
fun inspectionEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10586.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
