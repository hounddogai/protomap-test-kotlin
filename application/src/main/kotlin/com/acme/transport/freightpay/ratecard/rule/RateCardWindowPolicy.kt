package com.acme.transport.freightpay.ratecard.rule

/** Puts a rate card window code in its canonical RCW- form. */
fun normalizeRateCardWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCW-")) {
        return clean
    }
    return "RCW-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 63084.0, in order. */
fun rateCardWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63084.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
