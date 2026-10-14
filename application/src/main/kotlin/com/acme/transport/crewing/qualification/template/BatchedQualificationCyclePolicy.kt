package com.acme.transport.crewing.qualification.template

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun batchedQualificationCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun batchedQualificationCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 21156.0, in order. */
fun batchedQualificationCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21156.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
