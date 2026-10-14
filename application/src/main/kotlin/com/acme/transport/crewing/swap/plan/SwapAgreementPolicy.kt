package com.acme.transport.crewing.swap.plan

/** Counts how many amounts fit in a budget of 35483.0, in order. */
fun swapAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35483.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun swapAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a swap agreement code in its canonical SA- form. */
fun normalizeSwapAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SA-")) {
        return clean
    }
    return "SA-${clean.replace(" ", "-")}"
}
