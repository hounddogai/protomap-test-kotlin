package com.acme.transport.crewing.restbreak.breakvariance

/** Puts a tiered rest break manifest code in its canonical TRBM- form. */
fun normalizeTieredRestBreakManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TRBM-")) {
        return clean
    }
    return "TRBM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 61538.0, in order. */
fun tieredRestBreakManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61538.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
