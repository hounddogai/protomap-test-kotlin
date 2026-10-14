package com.acme.transport.emissions.idling.idlingmanifest

/** Puts a rolling idling ledger code in its canonical RIL- form. */
fun normalizeRollingIdlingLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RIL-")) {
        return clean
    }
    return "RIL-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 79787.0, in order. */
fun rollingIdlingLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79787.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
