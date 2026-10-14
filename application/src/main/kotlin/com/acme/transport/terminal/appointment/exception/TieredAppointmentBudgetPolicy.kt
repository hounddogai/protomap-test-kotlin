package com.acme.transport.terminal.appointment.exception

import com.acme.transport.kit.numbers.roundTo

fun tieredAppointmentBudgetBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 4776.0, in order. */
fun tieredAppointmentBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4776.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a tiered appointment budget score falls in. */
fun classifyTieredAppointmentBudget(score: Double): String {
    if (score < 8.1) {
        return "minimal"
    }
    if (score < 57.7) {
        return "low"
    }
    return "critical"
}
