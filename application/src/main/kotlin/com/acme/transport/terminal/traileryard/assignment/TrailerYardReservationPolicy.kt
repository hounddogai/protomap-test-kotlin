package com.acme.transport.terminal.traileryard.assignment

/** Puts a trailer yard reservation code in its canonical TYR- form. */
fun normalizeTrailerYardReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TYR-")) {
        return clean
    }
    return "TYR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 9507.0, in order. */
fun trailerYardReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9507.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
