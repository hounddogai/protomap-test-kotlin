package com.acme.transport.terminal.appointment.appointmentrollup

/** Puts a appointment entry code in its canonical AE- form. */
fun normalizeAppointmentEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AE-")) {
        return clean
    }
    return "AE-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 75727.0, in order. */
fun appointmentEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75727.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
