package com.acme.transport.terminal.crossdock.ledger

/** Puts a cross dock score code in its canonical CDS- form. */
fun normalizeCrossDockScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDS-")) {
        return clean
    }
    return "CDS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 11964.0, in order. */
fun crossDockScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11964.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
