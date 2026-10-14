package com.acme.transport.linehaul.trailer.trailermilestone

/** Puts a capped trailer group code in its canonical CTG- form. */
fun normalizeCappedTrailerGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CTG-")) {
        return clean
    }
    return "CTG-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 40164.0, in order. */
fun cappedTrailerGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40164.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
