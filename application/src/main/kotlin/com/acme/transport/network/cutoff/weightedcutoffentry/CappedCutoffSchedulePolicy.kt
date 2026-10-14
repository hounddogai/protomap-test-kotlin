package com.acme.transport.network.cutoff.weightedcutoffentry

/** Puts a capped cutoff schedule code in its canonical CCS- form. */
fun normalizeCappedCutoffScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CCS-")) {
        return clean
    }
    return "CCS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 2654.0, in order. */
fun cappedCutoffScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2654.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
