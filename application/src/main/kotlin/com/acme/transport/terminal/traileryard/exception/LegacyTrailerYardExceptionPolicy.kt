package com.acme.transport.terminal.traileryard.exception

/** Puts a legacy trailer yard exception code in its canonical LTYE- form. */
fun normalizeLegacyTrailerYardExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTYE-")) {
        return clean
    }
    return "LTYE-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 82137.0, in order. */
fun legacyTrailerYardExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82137.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
