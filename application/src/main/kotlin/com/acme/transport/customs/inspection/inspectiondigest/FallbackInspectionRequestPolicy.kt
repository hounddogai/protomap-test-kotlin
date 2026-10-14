package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fallbackInspectionRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.79
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 49047.0, in order. */
fun fallbackInspectionRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49047.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
