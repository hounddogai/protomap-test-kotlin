package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.numbers.roundTo

/** Names the band a capped appointment template score falls in. */
fun classifyCappedAppointmentTemplate(score: Double): String {
    if (score < 13.8) {
        return "minimal"
    }
    if (score < 57.2) {
        return "elevated"
    }
    if (score < 65.6) {
        return "medium"
    }
    if (score < 99.0) {
        return "low"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 75217.0, in order. */
fun cappedAppointmentTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75217.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedAppointmentTemplateBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
