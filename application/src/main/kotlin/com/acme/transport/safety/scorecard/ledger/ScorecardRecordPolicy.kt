package com.acme.transport.safety.scorecard.ledger

/** Counts how many amounts fit in a budget of 9233.0, in order. */
fun scorecardRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9233.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a scorecard record code in its canonical SR- form. */
fun normalizeScorecardRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SR-")) {
        return clean
    }
    return "SR-${clean.replace(" ", "-")}"
}
