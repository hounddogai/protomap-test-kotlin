package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Counts how many amounts fit in a budget of 79233.0, in order. */
fun rollingInspectionDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79233.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeRollingInspectionDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun rollingInspectionDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.71
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun rollingInspectionDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
