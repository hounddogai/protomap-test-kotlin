package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a utilization draft score falls in. */
fun classifyUtilizationDraft(score: Double): String {
    if (score < 9.1) {
        return "medium"
    }
    if (score < 27.8) {
        return "low"
    }
    if (score < 52.4) {
        return "ok"
    }
    if (score < 62.8) {
        return "minimal"
    }
    return "high"
}

fun mergeUtilizationDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun utilizationDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 32180.0, in order. */
fun utilizationDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32180.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
