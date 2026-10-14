package com.acme.transport.safety.nearmiss.estimate

/** Counts how many amounts fit in a budget of 63709.0, in order. */
fun nearMissEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63709.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a near miss estimate code in its canonical NME- form. */
fun normalizeNearMissEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NME-")) {
        return clean
    }
    return "NME-${clean.replace(" ", "-")}"
}
