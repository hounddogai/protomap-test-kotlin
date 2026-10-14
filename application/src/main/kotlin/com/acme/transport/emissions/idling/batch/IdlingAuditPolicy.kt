package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeIdlingAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 96115.0, in order. */
fun idlingAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 96115.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a idling audit score falls in. */
fun classifyIdlingAudit(score: Double): String {
    if (score < 8.0) {
        return "medium"
    }
    if (score < 13.7) {
        return "low"
    }
    if (score < 58.4) {
        return "minimal"
    }
    if (score < 62.9) {
        return "watch"
    }
    return "ok"
}
