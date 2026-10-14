package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

fun primaryRateCardDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a primary rate card draft code in its canonical PRCD- form. */
fun normalizePrimaryRateCardDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PRCD-")) {
        return clean
    }
    return "PRCD-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 83146.0, in order. */
fun primaryRateCardDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83146.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
