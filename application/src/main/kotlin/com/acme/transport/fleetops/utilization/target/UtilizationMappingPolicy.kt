package com.acme.transport.fleetops.utilization.target

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun utilizationMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun utilizationMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 8592.0, in order. */
fun utilizationMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8592.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
