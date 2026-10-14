package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeInspectionEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a inspection entry score falls in. */
fun classifyInspectionEntry(score: Double): String {
    if (score < 7.1) {
        return "medium"
    }
    if (score < 44.0) {
        return "high"
    }
    if (score < 66.2) {
        return "watch"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 43600.0, in order. */
fun inspectionEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43600.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun inspectionEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
