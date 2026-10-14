package com.acme.transport.terminal.appointment.appointmentallocation

/** Puts a pending appointment score code in its canonical PAS- form. */
fun normalizePendingAppointmentScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAS-")) {
        return clean
    }
    return "PAS-${clean.replace(" ", "-")}"
}

fun pendingAppointmentScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 30285.0, in order. */
fun pendingAppointmentScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30285.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
