package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeStrictInspectionBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 74743.0, in order. */
fun strictInspectionBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74743.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictInspectionBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a strict inspection batch score falls in. */
fun classifyStrictInspectionBatch(score: Double): String {
    if (score < 5.8) {
        return "elevated"
    }
    if (score < 92.9) {
        return "medium"
    }
    return "critical"
}
