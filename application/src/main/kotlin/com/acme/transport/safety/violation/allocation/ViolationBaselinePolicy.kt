package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeViolationBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 39460.0, in order. */
fun violationBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 39460.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a violation baseline score falls in. */
fun classifyViolationBaseline(score: Double): String {
    if (score < 43.5) {
        return "watch"
    }
    if (score < 64.6) {
        return "medium"
    }
    return "low"
}
