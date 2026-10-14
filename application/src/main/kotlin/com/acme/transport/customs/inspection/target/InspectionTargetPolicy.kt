package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a inspection target score falls in. */
fun classifyInspectionTarget(score: Double): String {
    if (score < 18.1) {
        return "elevated"
    }
    if (score < 73.5) {
        return "ok"
    }
    if (score < 79.4) {
        return "medium"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 33320.0, in order. */
fun inspectionTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33320.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeInspectionTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
