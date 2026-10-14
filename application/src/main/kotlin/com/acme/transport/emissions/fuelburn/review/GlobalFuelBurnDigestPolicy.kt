package com.acme.transport.emissions.fuelburn.review

/** Puts a global fuel burn digest code in its canonical GFBD- form. */
fun normalizeGlobalFuelBurnDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GFBD-")) {
        return clean
    }
    return "GFBD-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 51405.0, in order. */
fun globalFuelBurnDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51405.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
