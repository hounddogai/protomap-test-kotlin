package com.acme.transport.network.transittime.manifest

/** Counts how many amounts fit in a budget of 69286.0, in order. */
fun batchedTransitTimeRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 69286.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a batched transit time request code in its canonical BTTR- form. */
fun normalizeBatchedTransitTimeRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BTTR-")) {
        return clean
    }
    return "BTTR-${clean.replace(" ", "-")}"
}
