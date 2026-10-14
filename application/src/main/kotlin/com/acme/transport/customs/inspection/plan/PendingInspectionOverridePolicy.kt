package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun pendingInspectionOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun pendingInspectionOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.16
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 12240.0, in order. */
fun pendingInspectionOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12240.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
