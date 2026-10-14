package com.acme.transport.freightpay.ratecard.agreement

/** Puts a rate card adjustment code in its canonical RCA- form. */
fun normalizeRateCardAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCA-")) {
        return clean
    }
    return "RCA-${clean.replace(" ", "-")}"
}

fun rateCardAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 30752.0, in order. */
fun rateCardAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30752.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
