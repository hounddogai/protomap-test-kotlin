package com.acme.transport.customs.inspection.claim

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Counts how many amounts fit in a budget of 18864.0, in order. */
fun inspectionClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 18864.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun inspectionClaimBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun inspectionClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.59
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
