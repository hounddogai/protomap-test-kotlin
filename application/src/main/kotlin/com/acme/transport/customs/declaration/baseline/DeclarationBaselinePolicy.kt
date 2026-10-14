package com.acme.transport.customs.declaration.baseline

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun declarationBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun declarationBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Counts how many amounts fit in a budget of 64900.0, in order. */
fun declarationBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64900.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun declarationBaselineBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
