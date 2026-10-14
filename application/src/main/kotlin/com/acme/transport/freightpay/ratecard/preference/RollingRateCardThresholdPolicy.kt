package com.acme.transport.freightpay.ratecard.preference

/** Counts how many amounts fit in a budget of 8201.0, in order. */
fun rollingRateCardThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8201.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a rolling rate card threshold code in its canonical RRCT- form. */
fun normalizeRollingRateCardThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRCT-")) {
        return clean
    }
    return "RRCT-${clean.replace(" ", "-")}"
}
