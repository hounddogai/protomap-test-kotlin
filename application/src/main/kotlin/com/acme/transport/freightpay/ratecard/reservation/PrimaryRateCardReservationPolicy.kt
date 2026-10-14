package com.acme.transport.freightpay.ratecard.reservation

fun primaryRateCardReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 46687.0, in order. */
fun primaryRateCardReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46687.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a primary rate card reservation code in its canonical PRCR- form. */
fun normalizePrimaryRateCardReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PRCR-")) {
        return clean
    }
    return "PRCR-${clean.replace(" ", "-")}"
}
