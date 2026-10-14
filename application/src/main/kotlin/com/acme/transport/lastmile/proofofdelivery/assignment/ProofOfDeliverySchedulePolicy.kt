package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun proofOfDeliveryScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.98
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun proofOfDeliveryScheduleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 70800.0, in order. */
fun proofOfDeliveryScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70800.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
