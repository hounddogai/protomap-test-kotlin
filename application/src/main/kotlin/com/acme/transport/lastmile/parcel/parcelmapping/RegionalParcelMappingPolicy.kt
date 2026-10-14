package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 53432.0, in order. */
fun regionalParcelMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53432.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalParcelMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

fun regionalParcelMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
