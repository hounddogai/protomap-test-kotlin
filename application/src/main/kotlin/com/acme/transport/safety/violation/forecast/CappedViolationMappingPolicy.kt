package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun cappedViolationMappingBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun cappedViolationMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.04
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 94836.0, in order. */
fun cappedViolationMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94836.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
