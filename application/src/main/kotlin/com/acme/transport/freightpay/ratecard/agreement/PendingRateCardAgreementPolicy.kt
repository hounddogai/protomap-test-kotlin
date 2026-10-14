package com.acme.transport.freightpay.ratecard.agreement

import com.acme.transport.kit.numbers.roundTo

fun pendingRateCardAgreementBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 35441.0, in order. */
fun pendingRateCardAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35441.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a pending rate card agreement code in its canonical PRCA- form. */
fun normalizePendingRateCardAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PRCA-")) {
        return clean
    }
    return "PRCA-${clean.replace(" ", "-")}"
}
