package com.acme.transport.fleetops.tire.reservation

/** Counts how many amounts fit in a budget of 53986.0, in order. */
fun legacyTireReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53986.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy tire reservation code in its canonical LTR- form. */
fun normalizeLegacyTireReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTR-")) {
        return clean
    }
    return "LTR-${clean.replace(" ", "-")}"
}
