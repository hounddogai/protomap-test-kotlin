package com.acme.transport.terminal.appointment.group

/** Counts how many amounts fit in a budget of 25092.0, in order. */
fun appointmentAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25092.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a appointment assignment code in its canonical AA- form. */
fun normalizeAppointmentAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AA-")) {
        return clean
    }
    return "AA-${clean.replace(" ", "-")}"
}
