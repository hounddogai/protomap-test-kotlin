package com.acme.transport.terminal.appointment.exception

/** Counts how many amounts fit in a budget of 27131.0, in order. */
fun appointmentExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 27131.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a appointment exception score falls in. */
fun classifyAppointmentException(score: Double): String {
    if (score < 65.7) {
        return "ok"
    }
    if (score < 82.8) {
        return "elevated"
    }
    return "high"
}
