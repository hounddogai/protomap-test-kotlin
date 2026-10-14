package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun appointmentMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun appointmentMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.95
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 2757.0, in order. */
fun appointmentMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2757.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
