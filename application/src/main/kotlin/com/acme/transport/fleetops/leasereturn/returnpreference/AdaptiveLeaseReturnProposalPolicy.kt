package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun adaptiveLeaseReturnProposalBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 64814.0, in order. */
fun adaptiveLeaseReturnProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64814.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveLeaseReturnProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
