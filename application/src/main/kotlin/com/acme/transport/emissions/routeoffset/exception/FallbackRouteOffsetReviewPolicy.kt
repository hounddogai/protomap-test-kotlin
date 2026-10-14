package com.acme.transport.emissions.routeoffset.exception

/** Counts how many amounts fit in a budget of 83265.0, in order. */
fun fallbackRouteOffsetReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83265.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a fallback route offset review code in its canonical FROR- form. */
fun normalizeFallbackRouteOffsetReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FROR-")) {
        return clean
    }
    return "FROR-${clean.replace(" ", "-")}"
}
