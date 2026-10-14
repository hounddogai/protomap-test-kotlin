package com.acme.transport.emissions.idling.review

/** Puts a tiered idling review code in its canonical TIR- form. */
fun normalizeTieredIdlingReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TIR-")) {
        return clean
    }
    return "TIR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 93146.0, in order. */
fun tieredIdlingReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93146.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
