package com.acme.transport.network.transittime.manifest

/** Counts how many amounts fit in a budget of 38862.0, in order. */
fun strictTransitTimeNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38862.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a strict transit time notice code in its canonical STTN- form. */
fun normalizeStrictTransitTimeNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STTN-")) {
        return clean
    }
    return "STTN-${clean.replace(" ", "-")}"
}
