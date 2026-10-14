package com.acme.transport.terminal.crossdock.policy

/** Counts how many amounts fit in a budget of 13645.0, in order. */
fun scheduledCrossDockSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 13645.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a scheduled cross dock summary code in its canonical SCDS- form. */
fun normalizeScheduledCrossDockSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCDS-")) {
        return clean
    }
    return "SCDS-${clean.replace(" ", "-")}"
}
