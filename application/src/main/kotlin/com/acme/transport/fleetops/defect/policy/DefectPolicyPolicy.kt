package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defectPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 5415.0, in order. */
fun defectPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5415.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun defectPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
