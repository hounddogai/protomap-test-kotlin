package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeGlobalInspectionBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a global inspection batch score falls in. */
fun classifyGlobalInspectionBatch(score: Double): String {
    if (score < 12.6) {
        return "low"
    }
    if (score < 41.3) {
        return "medium"
    }
    if (score < 51.9) {
        return "high"
    }
    if (score < 75.7) {
        return "critical"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 51176.0, in order. */
fun globalInspectionBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51176.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalInspectionBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
