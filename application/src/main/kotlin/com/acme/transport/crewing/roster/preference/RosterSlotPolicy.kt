package com.acme.transport.crewing.roster.preference

/** Counts how many amounts fit in a budget of 48542.0, in order. */
fun rosterSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48542.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a roster slot code in its canonical RS- form. */
fun normalizeRosterSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RS-")) {
        return clean
    }
    return "RS-${clean.replace(" ", "-")}"
}
