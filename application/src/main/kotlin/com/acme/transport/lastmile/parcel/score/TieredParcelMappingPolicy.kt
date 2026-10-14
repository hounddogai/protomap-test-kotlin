package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun tieredParcelMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun tieredParcelMappingBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 23389.0, in order. */
fun tieredParcelMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23389.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
