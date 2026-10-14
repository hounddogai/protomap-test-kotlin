package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun servicePlanScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun servicePlanScheduleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 38430.0, in order. */
fun servicePlanScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38430.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
