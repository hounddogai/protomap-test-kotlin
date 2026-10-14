package com.acme.transport.network.transittime.scheduledtimebatch

/** Puts a default transit time entry code in its canonical DTTE- form. */
fun normalizeDefaultTransitTimeEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DTTE-")) {
        return clean
    }
    return "DTTE-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 36658.0, in order. */
fun defaultTransitTimeEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 36658.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
