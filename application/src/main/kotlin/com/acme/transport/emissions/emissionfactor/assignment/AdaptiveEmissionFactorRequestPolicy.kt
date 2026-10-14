package com.acme.transport.emissions.emissionfactor.assignment

/** Puts a adaptive emission factor request code in its canonical AEFR- form. */
fun normalizeAdaptiveEmissionFactorRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AEFR-")) {
        return clean
    }
    return "AEFR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 78093.0, in order. */
fun adaptiveEmissionFactorRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78093.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
